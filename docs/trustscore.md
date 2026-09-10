# VeriCart AI — How the AI Trust Score Works

The Trust Score is the heart of VeriCart AI. It answers one question shoppers ask every day:
**"Can I actually believe this product's ratings?"**

Instead of showing a single raw star average that blends genuine and fake feedback, VeriCart analyses
every review with AI and produces a **0–100 Trust Score** with a level
(Excellent / High / Medium / Low) and a plain-English explanation of *why* that score was given.

> Principle: **no black box.** Every score is explained by five weighted factors, and every suspicious
> review can be inspected with the AI's reason attached.

---

## 1. Where the score lives

The AI verdict is stored directly on the product and review rows:

| Table / column | Purpose |
| --- | --- |
| `product.trust_score` (`INT`) | Final 0–100 score |
| `product.trust_level` (`VARCHAR`) | `Excellent` / `High` / `Medium` / `Low` |
| `product.fake_review_count` (`INT`) | How many reviews were flagged as fake |
| `product.ai_summary` / `ai_summary_time` | Hunyuan-generated review summary |
| `review.sentiment` | AI sentiment: `POSITIVE` / `NEUTRAL` / `NEGATIVE` |
| `review.emotion` | AI emotion tag (e.g. happy, disappointed) |
| `review.fake_probability` (`DECIMAL`) | AI fake-review probability, 0–100 |
| `review.fake_reason` (`TEXT`) | Why the AI flagged the review |
| `review.is_flagged` (`TINYINT`) | `1` = suspicious, excluded from Trusted Rating |
| `review.order_id` | When present, the review is a **Verified Purchase** |
| `ai_analysis_log.task` | Logs each `sentiment` / `fake_detection` / `trust_score` / `summary` run |

---

## 2. Step 1 — Every review is read twice by AI

Each review posted to a product is analysed by **two specialist models**:

| Signal | Model | Output |
| --- | --- | --- |
| Fake / paid-review detection | **DeepSeek** (`detectFakeReview`) | `fake_probability` (0–100) + a short `fake_reason` |
| Sentiment | **Kimi** (`analyzeSentiment`) | `POSITIVE` / `NEUTRAL` / `NEGATIVE` + `emotion` tag |

The UI derives a badge from the result:

- `fake_probability < 30` → **Likely genuine**
- `fake_probability >= 30` → **Uncertain**
- flagged (`is_flagged = 1`) → **Suspicious** — removed from the Trusted Rating

A review whose `order_id` links to a confirmed order is also tagged **Verified Purchase**, which
counts more heavily in scoring.

---

## 3. Step 2 — The product-level analysis pipeline

The product analysis (`AiService.analyzeProduct`) runs automatically and orchestrates several models:

1. Load the product and all its reviews.
2. For every review that has no AI fields yet, run **DeepSeek fake detection** + **Kimi sentiment**
   and persist the verdicts.
3. Call **`AiGateway.generateTrustScore(product, reviews)`** — a *multi-model* call:
   - **DeepSeek** (`analyzeTrust`) reasons about overall authenticity and returns a `trustScore`;
   - **Kimi** (`analyzeSentimentBatch`) returns a `positiveRatio`;
   - the gateway blends them into the final 0–100 score.
4. Generate a **Hunyuan review summary** (resilient — if Hunyuan fails, a mock summary is used and the
   analysis is never aborted).
5. Count flagged reviews → `fakeReviewCount`.
6. Write `trust_score`, `trust_level`, `ai_summary`, `fake_review_count` back to the product.
7. Build the **explanation** (five weighted factors) and **topics**, and write an `AI_ANALYSIS`
   audit-log entry.

### How the numeric score itself is computed (AiGateway)

```
score = DeepSeek.trustScore                 // base 0-100 from authenticity reasoning
if Kimi available:
    pos = Kimi.positiveRatio                // 0..1 (or 0..100, normalised)
    score = score * 0.6 + pos * 40          // sentiment shapes up to 40 pts
if reviews < 3:  score = min(score, 60)     // too few reviews -> cannot be Excellent/High
clamp(score, 0, 100)
```

The level comes from thresholds:

| Score | Level |
| --- | --- |
| 85–100 | Excellent |
| 70–84  | High |
| 50–69  | Medium |
| 0–49   | Low |

---

## 4. Step 3 — The explainable five-factor breakdown

Because "the AI said so" is not enough, every product response also includes an **explanation**
(`AiService.buildTrustExplanation`) — five deterministic factors computed from the analysed reviews.
These are the bars you see on the Trust Score panel and product pages:

| Factor | Weight | Formula | Meaning |
| --- | --- | --- | --- |
| Review Authenticity | **30%** | `(total − flagged) / total × 100` | Share of reviews that passed fake detection |
| Customer Sentiment | **30%** | `positive / total × 100` | Tone of genuine reviews |
| Verified Purchases | **20%** | `verified / total × 100` | Reviews tied to real orders count more |
| Review Volume | **10%** | `min(100, total / 50 × 100)` | 50+ reviews = statistically reliable |
| Rating Consistency | **10%** | `max(0, 100 − σ × 40)` | Stable ratings score high; wild variance is a manipulation signal |

Where `σ` is the standard deviation of the star ratings (`σ = 2.5` drives this factor to 0).

The explained Trust Score equals the weighted blend of these five factor scores, making the displayed
number fully reproducible from the data.

---

## 5. Trusted Rating vs Raw Rating

A product has two averages:

- **Raw Rating** — `mean` of *all* star ratings (including fake ones).
- **Trusted Rating** — `mean` of star ratings **after flagged reviews are removed**.

Example: a product with a raw `4.33` but **2 reviews flagged as suspicious** shows a Trusted Rating
of **4.11**, plus a note explaining exactly what was removed and why.

---

## 6. When does the score update?

- **On demand** — a seller/admin clicks **Refresh Analysis**, or any client calls
  `POST /api/ai/analyze/{productId}`.
- **Automatically** — a backend scheduler (`AiScheduler`) wakes every ~30 seconds, finds reviews with
  `sentiment IS NULL`, and analyses them in batches (up to 20 products per cycle), cycling through the
  catalogue. Each analysed batch triggers a trust-score recompute for the affected product.
- **Transparently** — newly posted reviews show a **"Pending AI"** badge until the verdict lands
  (seconds later), then the badge becomes the AI verdict.

---

## 7. A worked example

Product with **50 reviews**:

- 50 reviews total, **5 flagged** by DeepSeek, **35 positive** per Kimi, **25 verified** (`order_id`),
  ratings mostly 4–5 stars → `σ = 0.5`.

| Factor | Score | Weight | Contribution |
| --- | --- | --- | --- |
| Authenticity | (50−5)/50×100 = 90 | 0.30 | 27 |
| Sentiment | 35/50×100 = 70 | 0.30 | 21 |
| Verified | 25/50×100 = 50 | 0.20 | 10 |
| Volume | min(100, 50/50×100) = 100 | 0.10 | 10 |
| Consistency | max(0, 100 − 0.5×40) = 80 | 0.10 | 8 |
| **Trust Score** | **76** | | **High** |

Now suppose 40 of the 50 reviews were flagged: Authenticity collapses to 20 and the Trusted Rating
drops dramatically — which is exactly what an honest shopper should see before clicking "buy".

---

## 8. Try it yourself

```bash
# Full re-analysis of a product (UI: product page → "Refresh Analysis")
curl -X POST http://localhost:8080/api/ai/analyze/1

# Read the stored result
GET http://localhost:8080/api/products/1            # includes trustScore, trustLevel, aiSummary
GET http://localhost:8080/api/reviews/product/1/trust   # raw vs trusted metrics
```

The seed data deliberately includes fake reviews so you can watch the AI find them and see the
Trusted Rating drop below the Raw Rating.

---

## 9. Edge cases & resilience

- **Fewer than 3 reviews** → score is capped at 60 (never "Excellent" on one or two reviews).
- **Provider outage** → `AiGateway` catches errors per model and degrades gracefully: a missing
  Hunyuan summary falls back to mock; chat falls back Kimi → DeepSeek → mock. The product never fails.
- **Mock mode** (`AI_MOCK_ENABLED=true`) runs the whole pipeline offline with deterministic heuristics
  so demos and tests never need API keys.
- Every AI action is recorded in `audit_log` (task `AI_ANALYSIS` / `AI_ANALYSIS_FAILED`) for
  traceability (FR-069 / FR-070).

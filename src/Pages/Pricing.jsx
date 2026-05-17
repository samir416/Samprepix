import "../styles/Pricing.css";
import Navbar from "../Components/Common/Navbar";

import {
  FaCheck,
  FaBolt,
  FaCrown,
  FaGift,
  FaHistory,
  FaArrowLeft,
  FaCheckCircle,
  FaTimesCircle,
} from "react-icons/fa";

import { useState } from "react";

export default function Pricing() {

  const [discountApplied, setDiscountApplied] = useState(false);

  const [showHistory, setShowHistory] = useState(false);

  const [activePlan, setActivePlan] = useState("");

  const [referralInput, setReferralInput] = useState("");

  const [referralMessage, setReferralMessage] = useState("");

  const [referralError, setReferralError] = useState(false);

  /* VALID REFERRAL */

  const validReferral = "SAMIRPRO50";

  /* APPLY REFERRAL */

  const applyReferral = () => {

    if (referralInput.trim() === "") {

      setReferralError(true);

      setDiscountApplied(false);

      setReferralMessage(
        "Please enter a referral code."
      );

      return;
    }

    if (
      referralInput.toUpperCase() ===
      validReferral
    ) {

      setReferralError(false);

      setDiscountApplied(true);

      setReferralMessage(
        "Referral applied successfully. 50% discount unlocked."
      );

    } else {

      setReferralError(true);

      setDiscountApplied(false);

      setReferralMessage(
        "Invalid referral code. Please try again."
      );
    }
  };

  /* PASTE */

  const pasteReferral = async () => {

    const text =
      await navigator.clipboard.readText();

    setReferralInput(text);
  };

  /* PLAN */

  const activatePlan = (plan) => {

    setActivePlan(plan);
  };

  /* HISTORY PAGE */

  if (showHistory) {

    return (
        

      <div className="history-page">

        <Navbar />

        <div className="history-wrapper">

          <button
            className="back-btn"
            onClick={() => setShowHistory(false)}
          >

            <FaArrowLeft />

            Back

          </button>

          <div className="history-header">

            <h1>
              Payment History
            </h1>

            <p>
              Track subscriptions,
              invoices and transactions.
            </p>

          </div>

          <div className="history-table">

            <div className="history-row history-head">

              <span>Plan</span>

              <span>Status</span>

              <span>Amount</span>

              <span>Date</span>

            </div>

            <div className="history-row">

              <span>Pro Plan</span>

              <span className="success">
                Completed
              </span>

              <span>₹99</span>

              <span>16 May 2026</span>

            </div>

            <div className="history-row">

              <span>Elite Plan</span>

              <span className="success">
                Completed
              </span>

              <span>₹249</span>

              <span>17 May 2026</span>

            </div>

          </div>

        </div>

      </div>
    );
  }

  return (

    <div className="pricing-page">

      <Navbar />

      {/* HERO */}

      <section className="pricing-hero">

        <span className="pricing-badge">

          Built To Transform Students Into Top Candidates

        </span>

        <h1>

          Premium AI Placement
          <br />
          Preparation Platform

        </h1>

        <p>

          Unlock AI interviews, ATS resume analysis,
          coding preparation, analytics dashboards,
          company packs and personalized roadmaps.

        </p>

      </section>

      {/* PRICING */}

      <section className="pricing-grid">

        {/* FREE */}

        <div className="pricing-card">

          <div className="plan-tag">
            Starter
          </div>

          <h2>
            Free
          </h2>

          <p className="card-subtitle">

            Perfect for exploring the platform.

          </p>

          <div className="plan-features">

            <div>
              <FaCheck />
              3 AI interviews/day
            </div>

            <div>
              <FaCheck />
              5 resume scans/month
            </div>

            <div>
              <FaCheck />
              Basic ATS analysis
            </div>

            <div>
              <FaCheck />
              Limited coding arena
            </div>

          </div>

          <button
            className="plan-btn"
            onClick={() => activatePlan("Starter Plan")}
          >

            Start Free

          </button>

        </div>

        {/* PRO */}

        <div className="pricing-card pro-card">

          <div className="popular-badge">

            MOST POPULAR

          </div>

          <div className="plan-tag pro-tag">
            Pro
          </div>

          <h2>

            {discountApplied ? (
              <>
                <span className="old-price">
                  ₹199
                </span>

                ₹99
              </>
            ) : (
              "₹199"
            )}

          </h2>

          <p className="card-subtitle">

            Best for serious placement preparation.

          </p>

          <div className="plan-features">

            <div>
              <FaBolt />
              Unlimited AI interviews
            </div>

            <div>
              <FaBolt />
              Full resume analyzer
            </div>

            <div>
              <FaBolt />
              AI coding hints
            </div>

            <div>
              <FaBolt />
              Personalized roadmap
            </div>

            <div>
              <FaBolt />
              Company preparation packs
            </div>

          </div>

          <button
            className="plan-btn pro-btn"
            onClick={() => activatePlan("Pro Plan")}
          >

            Upgrade To Pro

          </button>

        </div>

        {/* ELITE */}

        <div className="pricing-card">

          <div className="plan-tag elite-tag">
            Elite
          </div>

          <h2>

            {discountApplied ? (
              <>
                <span className="old-price">
                  ₹499
                </span>

                ₹249
              </>
            ) : (
              "₹499"
            )}

          </h2>

          <p className="card-subtitle">

            Advanced AI career acceleration suite.

          </p>

          <div className="plan-features">

            <div>
              <FaCrown />
              Google & Amazon packs
            </div>

            <div>
              <FaCrown />
              HR round simulations
            </div>

            <div>
              <FaCrown />
              Premium analytics
            </div>

            <div>
              <FaCrown />
              Future AI tools access
            </div>

          </div>

          <button
            className="plan-btn elite-btn"
            onClick={() => activatePlan("Elite Plan")}
          >

            Unlock Elite

          </button>

        </div>

      </section>

      {/* ACTIVE PLAN */}

      {activePlan && (

        <div className="plan-activated-box">

          <FaCheckCircle />

          {activePlan} activated successfully.

        </div>

      )}

      {/* REFERRAL */}

      <section className="referral-section">

        <div className="referral-left">

          <span>

            <FaGift />

            Referral Rewards

          </span>

          <h2>

            Invite Friends &
            Unlock Discounts

          </h2>

          <p>

            Enter your referral code and unlock
            premium discounts and bonus AI access.

          </p>

        </div>

        <div className="referral-right">

          <div className="referral-box">

            <input
              type="text"
              placeholder="Enter referral code"
              value={referralInput}
              onChange={(e) =>
                setReferralInput(e.target.value)
              }
            />

            <button onClick={pasteReferral}>

              Paste

            </button>

          </div>

          <button
            className="apply-btn"
            onClick={applyReferral}
          >

            Apply Referral Code

          </button>

          {referralMessage && (

            <div
              className={
                referralError
                  ? "referral-error"
                  : "referral-success"
              }
            >

              {referralError ? (
                <FaTimesCircle />
              ) : (
                <FaCheckCircle />
              )}

              {referralMessage}

            </div>

          )}

        </div>

      </section>

      {/* HISTORY */}

      <section className="payment-history-section">

        <div className="history-card">

          <div className="history-icon">

            <FaHistory />

          </div>

          <div className="history-content">

            <h3>
              Payment History
            </h3>

            <p>

              View subscriptions,
              invoices and transactions.

            </p>

          </div>

          <button
            className="history-open-btn"
            onClick={() => setShowHistory(true)}
          >

            Open History

          </button>

        </div>

      </section>

    </div>
  );
}
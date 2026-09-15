const fs = require('fs');
let code = fs.readFileSync('frontend/src/Pages/SubscriptionPage.jsx', 'utf-8');

// Replace Razorpay logic with Cashfree
code = code.replace(/initiateRazorpayCheckout/g, 'initiateCashfreeCheckout');
code = code.replace(/"razorpay"/g, '"cashfree"');
code = code.replace(/razorpayOrderId/g, 'cashfreeOrderId');
code = code.replace(/razorpayResponse\.razorpay_order_id/g, 'cashfreeResponse.order_id');

// Replace checkout implementation
const oldCheckout = `    // =========================================================
    // RAZORPAY CHECKOUT
    // =========================================================

    const initiateCashfreeCheckout = (
        order,
        plan
    ) => {

        if (!window.Razorpay) {

            setError(
                "Razorpay checkout is not loaded. Please refresh the page and try again."
            );

            setProcessing(false);

            return;
        }

        const storedUser =
            localStorage.getItem("user");

        let user;

        try {
            user = JSON.parse(storedUser);

        } catch {
            user = {};
        }

        const options = {

            key: order.key,

            /*
             * Razorpay expects amount in paise.
             * Backend returns amount in major currency unit.
             */
            amount:
                Number(order.amount || 0) * 100,

            currency:
                order.currency || currency,

            name: "Samprepix",

            description:
                \`Subscription to \${plan.name} Plan\`,

            order_id:
                order.cashfreeOrderId,

            handler: function (response) {

                verifyAndActivate(
                    response,
                    plan
                );
            },

            prefill: {
                name: user.name || "User",
                email: user.email || ""
            },

            theme: {
                color: "#1d4ed8"
            },

            modal: {

                ondismiss: function () {

                    setProcessing(false);
                }
            }
        };

        const razorpay =
            new window.Razorpay(options);

        razorpay.on(
            "payment.failed",
            async function (response) {

                try {

                    await markPaymentFailed(
                        order.cashfreeOrderId
                    );

                } catch (error) {

                    console.error(
                        "Failed to mark payment as failed:",
                        error
                    );
                }

                setError(
                    "Payment failed: " +
                    (
                        response?.error?.description ||
                        "Please try again."
                    )
                );

                setProcessing(false);
            }
        );

        razorpay.open();
    };`;

const newCheckout = `    // =========================================================
    // CASHFREE CHECKOUT
    // =========================================================

    const initiateCashfreeCheckout = (order, plan) => {
        if (!window.Cashfree) {
            setError("Cashfree SDK is not loaded. Please refresh the page.");
            setProcessing(false);
            return;
        }

        const cashfree = window.Cashfree({
            mode: order.environment === "PRODUCTION" ? "production" : "sandbox"
        });

        const checkoutOptions = {
            paymentSessionId: order.paymentSessionId,
            redirectTarget: "_modal",
        };

        cashfree.checkout(checkoutOptions).then(function (result) {
            if (result.error) {
                console.error("Cashfree Payment Error:", result.error);
                markPaymentFailed(order.cashfreeOrderId).catch(console.error);
                setError(result.error.message || "Payment failed or cancelled.");
                setProcessing(false);
            }
            if (result.paymentDetails) {
                verifyAndActivate({ order_id: order.cashfreeOrderId }, plan);
            }
            if (result.redirect) {
                console.log("Cashfree redirection");
            }
        });
    };`;

code = code.replace(oldCheckout, newCheckout);

const verifyOld = `                    razorpay_order_id:
                        cashfreeResponse.order_id,

                    razorpay_payment_id:
                        cashfreeResponse.razorpay_payment_id,

                    razorpay_signature:
                        cashfreeResponse.razorpay_signature`;

const verifyNew = `                    order_id: cashfreeResponse.order_id`;

code = code.replace(verifyOld, verifyNew);

code = code.replace(/razorpayResponse/g, 'cashfreeResponse');
code = code.replace(/via Razorpay Test Mode/g, 'via Cashfree Test Mode');

fs.writeFileSync('frontend/src/Pages/SubscriptionPage.jsx', code, 'utf-8');
console.log('Successfully updated SubscriptionPage.jsx for Cashfree');

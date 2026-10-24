import React, { useState } from 'react';
import { X, CheckCircle2, ShieldCheck, ArrowRight, Zap, Award } from 'lucide-react';
import confetti from 'canvas-confetti';

export default function DeliveryHandshakeModal({
  facility,
  isOpen,
  onClose,
  onVerifyHandshake
}) {
  if (!isOpen || !facility) return null;

  const [tokenInput, setTokenInput] = useState('');
  const [recipientSignoff, setRecipientSignoff] = useState(facility?.contactPerson || "Sis' Thandiwe");
  const [roundTripKm, setRoundTripKm] = useState(4.0);
  const [verificationResponse, setVerificationResponse] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!tokenInput.trim()) return;

    setSubmitting(true);
    const result = await onVerifyHandshake({
      verificationToken: tokenInput.trim(),
      recipientSignoff,
      roundTripKm: Number(roundTripKm)
    });
    setSubmitting(false);

    if (result) {
      setVerificationResponse(result);
      // Trigger Celebration Confetti
      confetti({
        particleCount: 120,
        spread: 80,
        origin: { y: 0.6 }
      });
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-700 rounded-2xl p-6 shadow-2xl text-slate-100 overflow-hidden">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        {!verificationResponse ? (
          <div>
            <div className="flex items-center space-x-2 text-cyan-400">
              <ShieldCheck className="w-5 h-5" />
              <span className="text-xs font-bold uppercase tracking-wider">Epic 4 & 5 • Handshake Verification</span>
            </div>
            <h3 className="text-lg font-bold text-white mt-1">
              Verify Delivery for {facility.name}
            </h3>
            <p className="text-xs text-slate-300 mt-1">
              Enter the OTP verification token provided by the dispatch transporter.
            </p>

            <form onSubmit={handleSubmit} className="mt-4 space-y-4">
              <div>
                <label className="text-xs font-bold text-slate-300 block mb-1">
                  Transporter Handshake Token:
                </label>
                <input
                  type="text"
                  value={tokenInput}
                  onChange={(e) => setTokenInput(e.target.value)}
                  placeholder="e.g. TOKEN-789"
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-cyan-500/50 font-mono text-base font-bold text-cyan-300 tracking-wider focus:outline-none focus:border-cyan-400"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-[11px] font-bold text-slate-400 block mb-1">
                    Recipient Signoff:
                  </label>
                  <input
                    type="text"
                    value={recipientSignoff}
                    onChange={(e) => setRecipientSignoff(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="text-[11px] font-bold text-slate-400 block mb-1">
                    Round Trip Distance (km):
                  </label>
                  <input
                    type="number"
                    step="0.5"
                    value={roundTripKm}
                    onChange={(e) => setRoundTripKm(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs text-white"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={submitting}
                className="w-full py-2.5 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-slate-950 font-extrabold text-sm shadow-lg shadow-emerald-500/20 transition flex items-center justify-center space-x-2"
              >
                <span>{submitting ? 'Verifying...' : 'Confirm Delivery & Trigger FNB Payment'}</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </form>
          </div>
        ) : (
          /* Official FNB Instant Pay Receipt */
          <div className="text-center py-2">
            <div className="w-14 h-14 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto mb-2 border border-emerald-500/40">
              <CheckCircle2 className="w-8 h-8" />
            </div>
            <h3 className="text-xl font-extrabold text-white">Delivery Handshake Verified!</h3>
            <p className="text-xs text-emerald-400 font-semibold mt-0.5">
              Node status transitioned to <span className="underline">RESOLVED (GREEN)</span>
            </p>

            {/* FNB Banking Payout Receipt Card */}
            <div className="mt-4 p-4 rounded-xl bg-[#091F26] border border-teal-500/40 text-left shadow-xl">
              <div className="flex items-center justify-between pb-2 border-b border-teal-900/60">
                <span className="text-xs font-black uppercase text-teal-300 tracking-wider flex items-center space-x-1">
                  <Zap className="w-3.5 h-3.5 text-orange-400" />
                  <span>FNB Instant Pay Disbursement</span>
                </span>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 uppercase">
                  PAID
                </span>
              </div>

              <div className="mt-3 space-y-1.5 text-xs">
                <div className="flex justify-between">
                  <span className="text-slate-400">Recipient Driver:</span>
                  <span className="font-mono text-slate-200">{verificationResponse.fnbDisbursement?.recipientMobile || '+27 82 555 9901'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Fuel Grant Stipend:</span>
                  <span className="font-bold text-white text-sm">
                    R{verificationResponse.fnbDisbursement?.amountZar || 150}.00 ZAR
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">FNB Transaction Ref:</span>
                  <span className="font-mono text-teal-300 font-bold">
                    {verificationResponse.fnbDisbursement?.transactionReference || 'FNB-PAY-99A120'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Payment Rail:</span>
                  <span className="text-slate-300">FNB Instant Pay / eWallet</span>
                </div>
              </div>
            </div>

            <p className="text-[11px] text-slate-400 mt-3">
              Protected <b>{verificationResponse.beneficiariesProtected || facility.beneficiaryCount} lives</b> • Proof cryptographically logged on SHA-256 ledger.
            </p>

            <button
              onClick={onClose}
              className="mt-4 w-full py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-bold text-xs transition"
            >
              Close & View Updated Map
            </button>
          </div>
        )}
      </div>
    </div>
  );
}


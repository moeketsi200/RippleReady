import React, { useState } from 'react';
import { X, Droplet, ShieldCheck, Check, Copy, ArrowRight, Truck } from 'lucide-react';

export default function MutualAidPledgeModal({
  facility,
  resources,
  isOpen,
  onClose,
  onConfirmPledge
}) {
  if (!isOpen || !facility) return null;

  const [selectedResourceId, setSelectedResourceId] = useState(resources[0]?.id || 'res-1');
  const [pledgeUnits, setPledgeUnits] = useState(500);
  const [driverMobile, setDriverMobile] = useState('+27 82 555 9901');
  const [copied, setCopied] = useState(false);
  const [generatedToken, setGeneratedToken] = useState(null);

  const selectedResource = resources.find(r => r.id === selectedResourceId) || resources[0];

  const handleSubmit = (e) => {
    e.preventDefault();
    const token = "TOKEN-" + Math.floor(100 + Math.random() * 899);
    setGeneratedToken(token);
    onConfirmPledge({
      resourceId: selectedResourceId,
      facilityId: facility.id,
      allocatedUnits: pledgeUnits,
      pledgerContact: selectedResource?.contactPhone || '+27 82 555 0192',
      driverMobile,
      verificationToken: token,
      notes: `Disaster mutual aid run to ${facility.name}`
    });
  };

  const handleCopy = () => {
    if (generatedToken) {
      navigator.clipboard.writeText(generatedToken);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-700 rounded-2xl p-6 shadow-2xl text-slate-100 overflow-hidden">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        {!generatedToken ? (
          <div>
            <div className="flex items-center space-x-2 text-teal-400">
              <Droplet className="w-5 h-5" />
              <span className="text-xs font-bold uppercase tracking-wider">Epic 3 • Mutual Aid Sourcing</span>
            </div>
            <h3 className="text-lg font-bold text-white mt-1">
              Mobilize Aid for {facility.name}
            </h3>
            <p className="text-xs text-slate-300 mt-1">
              Primary Need: <b className="text-teal-300">{facility.primaryNeed}</b> ({facility.beneficiaryCount} people at risk)
            </p>

            <form onSubmit={handleSubmit} className="mt-4 space-y-4">
              {/* Sourcing Provider Selection */}
              <div>
                <label className="text-xs font-bold text-slate-300 block mb-2">
                  Select Nearby Surplus Water Reserve:
                </label>
                <div className="space-y-2">
                  {resources.map((res) => (
                    <label
                      key={res.id}
                      className={`flex items-center justify-between p-3 rounded-xl border cursor-pointer transition ${
                        selectedResourceId === res.id
                          ? 'bg-teal-950/40 border-teal-500 ring-1 ring-teal-500'
                          : 'bg-slate-800/60 border-slate-700 hover:border-slate-600'
                      }`}
                    >
                      <div className="flex items-center space-x-3">
                        <input
                          type="radio"
                          name="resourceChoice"
                          value={res.id}
                          checked={selectedResourceId === res.id}
                          onChange={() => setSelectedResourceId(res.id)}
                          className="accent-teal-500"
                        />
                        <div>
                          <h4 className="text-xs font-bold text-white">{res.providerName}</h4>
                          <span className="text-[11px] text-slate-400">
                            {res.distanceKm} km away • {res.resourceTypeDisplay || res.resourceType}
                          </span>
                        </div>
                      </div>
                      <div className="text-right">
                        <span className="text-xs font-black text-teal-400 block">
                          {res.availableCapacity} L
                        </span>
                        <span className="text-[10px] text-slate-400">available</span>
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              {/* Volume Slider */}
              <div className="p-3 rounded-xl bg-slate-800/40 border border-slate-700/60">
                <div className="flex justify-between items-center text-xs font-bold mb-1">
                  <span className="text-slate-300">Volume to Pledge:</span>
                  <span className="text-teal-400 font-extrabold text-sm">{pledgeUnits} Litres</span>
                </div>
                <input
                  type="range"
                  min="100"
                  max={Math.min(1500, selectedResource?.availableCapacity || 1500)}
                  step="50"
                  value={pledgeUnits}
                  onChange={(e) => setPledgeUnits(Number(e.target.value))}
                  className="w-full accent-teal-500 cursor-pointer"
                />
                <div className="flex justify-between text-[10px] text-slate-500 mt-1">
                  <span>100 L</span>
                  <span>{Math.min(1500, selectedResource?.availableCapacity || 1500)} L</span>
                </div>
              </div>

              {/* Driver Mobile for FNB Payout */}
              <div>
                <label className="text-xs font-bold text-slate-300 block mb-1">
                  Volunteer Driver Mobile (for FNB eWallet R150 Stipend):
                </label>
                <input
                  type="text"
                  value={driverMobile}
                  onChange={(e) => setDriverMobile(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs text-white focus:outline-none focus:border-teal-500"
                  placeholder="+27 82 555 9901"
                  required
                />
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                className="w-full py-2.5 rounded-xl bg-gradient-to-r from-teal-500 to-teal-600 hover:from-teal-400 hover:to-teal-500 text-slate-950 font-extrabold text-sm shadow-lg shadow-teal-500/20 transition flex items-center justify-center space-x-2"
              >
                <span>Lock Capacity & Generate Token</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </form>
          </div>
        ) : (
          <div className="text-center py-4">
            <div className="w-14 h-14 rounded-full bg-teal-500/20 text-teal-400 flex items-center justify-center mx-auto mb-3 border border-teal-500/40">
              <Truck className="w-7 h-7" />
            </div>
            <h3 className="text-lg font-extrabold text-white">Capacity Reserved & Dispatched!</h3>
            <p className="text-xs text-slate-300 mt-1 max-w-sm mx-auto">
              <b>{pledgeUnits} Litres</b> locked from <i>{selectedResource?.providerName}</i> for <b>{facility.name}</b>.
            </p>

            {/* Generated Token Display */}
            <div className="my-5 p-4 rounded-xl bg-slate-950 border border-teal-500/50 inline-block shadow-inner">
              <span className="text-[10px] font-bold uppercase text-slate-400 tracking-wider block mb-1">
                Handshake Verification Token (OTP)
              </span>
              <div className="flex items-center justify-center space-x-3">
                <span className="font-mono text-2xl font-black text-teal-300 tracking-widest">
                  {generatedToken}
                </span>
                <button
                  onClick={handleCopy}
                  className="p-1.5 rounded-lg bg-teal-500/20 hover:bg-teal-500/30 text-teal-300 transition"
                  title="Copy token"
                >
                  {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <p className="text-[11px] text-slate-400 mb-4">
              Driver will present this token to <b>{facility.contactPerson}</b> ({facility.contactPhone}) upon arrival to authorize the FNB fuel stipend.
            </p>

            <button
              onClick={onClose}
              className="w-full py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-bold text-xs transition"
            >
              Back to Live Map
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

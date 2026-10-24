import React from 'react';
import { AlertCircle, Clock, Users, DollarSign, CheckCircle2, Truck, Sparkles, ArrowRight, MapPin } from 'lucide-react';

export default function CascadeSummaryPanel({
  facilities,
  simulationResult,
  selectedFacilityId,
  onFocusFacility,
  onOpenPledge,
  onOpenVerify
}) {
  const threatenedCount = facilities.filter(f => f.status === 'THREATENED').length;
  const inTransitCount = facilities.filter(f => f.status === 'DISPATCHED').length;
  const resolvedCount = facilities.filter(f => f.status === 'RESOLVED').length;

  return (
    <div className="flex flex-col h-full bg-slate-900/90 rounded-2xl border border-slate-800 p-4 shadow-xl overflow-hidden">
      {/* Header Metric Ribbon */}
      <div className="pb-3 border-b border-slate-800">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <span className="p-1.5 rounded-lg bg-teal-500/10 text-teal-400">
              <Sparkles className="w-4 h-4" />
            </span>
            <h2 className="text-sm font-bold text-white uppercase tracking-wider">
              Downstream Cascade Risks
            </h2>
          </div>
          <span className="text-[11px] font-mono text-teal-400 font-semibold px-2 py-0.5 rounded bg-teal-950 border border-teal-800/40">
            Computed in {simulationResult?.executionTimeMs || 2}ms
          </span>
        </div>

        {/* Status Counters */}
        <div className="grid grid-cols-3 gap-2 mt-3">
          <div className="p-2.5 rounded-xl bg-red-950/40 border border-red-900/40 text-center">
            <span className="text-[10px] font-bold text-red-300 uppercase block">Threatened</span>
            <span className="text-xl font-extrabold text-red-400">{threatenedCount}</span>
          </div>
          <div className="p-2.5 rounded-xl bg-cyan-950/40 border border-cyan-900/40 text-center">
            <span className="text-[10px] font-bold text-cyan-300 uppercase block">In Transit</span>
            <span className="text-xl font-extrabold text-cyan-400">{inTransitCount}</span>
          </div>
          <div className="p-2.5 rounded-xl bg-emerald-950/40 border border-emerald-900/40 text-center">
            <span className="text-[10px] font-bold text-emerald-300 uppercase block">Resolved</span>
            <span className="text-xl font-extrabold text-emerald-400">{resolvedCount}</span>
          </div>
        </div>
      </div>

      {/* Facilities List by Priority Tier */}
      <div className="flex-1 overflow-y-auto mt-3 space-y-3 pr-1">
        {facilities.map((fac) => {
          const isResolved = fac.status === 'RESOLVED';
          const isDispatched = fac.status === 'DISPATCHED';
          const isSelected = selectedFacilityId === fac.id;

          return (
            <div
              key={fac.id}
              onClick={() => onFocusFacility && onFocusFacility(fac)}
              title="Click to focus & pan map to this facility"
              className={`p-3 rounded-xl border transition-all cursor-pointer ${
                isSelected
                  ? 'ring-2 ring-teal-400 bg-teal-950/40 border-teal-500 shadow-lg shadow-teal-500/20 scale-[1.01]'
                  : isResolved
                  ? 'bg-emerald-950/20 border-emerald-800/40 shadow-sm hover:border-emerald-600'
                  : isDispatched
                  ? 'bg-cyan-950/20 border-cyan-800/40 shadow-sm hover:border-cyan-600'
                  : fac.urgencyLevel === 'CRITICAL'
                  ? 'bg-red-950/20 border-red-800/50 hover:border-red-600'
                  : 'bg-slate-800/50 border-slate-700/60 hover:border-slate-500'
              }`}
            >
              <div className="flex items-start justify-between gap-2">
                <div>
                  <div className="flex items-center space-x-1.5 flex-wrap gap-1">
                    <span className={`text-[9px] font-black uppercase px-1.5 py-0.5 rounded ${
                      fac.priorityTier === 1
                        ? 'bg-red-500/20 text-red-300 border border-red-500/30'
                        : fac.priorityTier === 2
                        ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                        : 'bg-teal-500/20 text-teal-300 border border-teal-500/30'
                    }`}>
                      Tier {fac.priorityTier}: {fac.tierDisplay || 'Priority'}
                    </span>
                    <span className="text-xs text-slate-400">• {fac.distanceKm} km away</span>
                    {isSelected && (
                      <span className="text-[9px] font-extrabold text-teal-300 bg-teal-900/70 px-1.5 py-0.5 rounded border border-teal-400/50 flex items-center space-x-1">
                        <MapPin className="w-2.5 h-2.5 text-teal-300" />
                        <span>On Map</span>
                      </span>
                    )}
                  </div>
                  <h3 className="font-bold text-sm text-white mt-1">{fac.name}</h3>
                </div>

                <span className={`text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-full ${
                  isResolved
                    ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40'
                    : isDispatched
                    ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/40'
                    : 'bg-red-500/20 text-red-400 border border-red-500/40'
                }`}>
                  {fac.status}
                </span>
              </div>

              <p className="text-xs text-slate-300 mt-1.5 font-medium">
                {fac.primaryNeed}
              </p>

              {/* Buffer & Loss Metrics */}
              <div className="grid grid-cols-2 gap-2 mt-2 pt-2 border-t border-slate-800 text-[11px] text-slate-400">
                <div className="flex items-center space-x-1.5">
                  <Clock className="w-3.5 h-3.5 text-amber-400" />
                  <span>Buffer: <b className="text-white">{fac.bufferHours}h</b> ({fac.bufferHours * 60}m)</span>
                </div>
                <div className="flex items-center space-x-1.5">
                  <Users className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Impact: <b className="text-white">{fac.beneficiaryCount} people</b></span>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="mt-2.5 pt-2 flex items-center justify-between gap-2">
                {isResolved ? (
                  <div className="flex items-center space-x-1.5 text-emerald-400 text-xs font-semibold">
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Safe & Operational</span>
                  </div>
                ) : isDispatched ? (
                  <button
                    onClick={() => onOpenVerify(fac)}
                    className="w-full py-1.5 px-3 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white font-bold text-xs flex items-center justify-center space-x-1.5 shadow-md shadow-cyan-600/20 transition"
                  >
                    <Truck className="w-3.5 h-3.5 animate-bounce" />
                    <span>Confirm Handshake Token</span>
                  </button>
                ) : (
                  <button
                    onClick={() => onOpenPledge(fac)}
                    className="w-full py-1.5 px-3 rounded-lg bg-teal-600 hover:bg-teal-500 text-white font-bold text-xs flex items-center justify-center space-x-1.5 shadow-md shadow-teal-600/20 transition"
                  >
                    <span>Mobilize Mutual Aid</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}


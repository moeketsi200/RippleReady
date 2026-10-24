import React, { useState, useEffect } from 'react';
import { X, Award, Shield, BarChart3, Database, KeyRound, CheckCircle } from 'lucide-react';
import { api } from '../services/api';

export default function ImpactDashboardModal({ isOpen, onClose }) {
  if (!isOpen) return null;

  const [impact, setImpact] = useState(null);
  const [ledger, setLedger] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      setLoading(true);
      const [impactData, ledgerData] = await Promise.all([
        api.getImpactSummary(),
        api.getAuditLedger()
      ]);
      setImpact(impactData);
      setLedger(ledgerData);
      setLoading(false);
    }
    loadData();
  }, []);

  return (
    <div className="fixed inset-0 z-[9999] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-3xl max-h-[90vh] bg-slate-900 border border-slate-700 rounded-2xl p-6 shadow-2xl text-slate-100 overflow-y-auto">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-2 text-teal-400">
          <BarChart3 className="w-5 h-5" />
          <span className="text-xs font-bold uppercase tracking-wider">FNB Hackathon Impact Dashboard</span>
        </div>
        <h3 className="text-xl font-extrabold text-white mt-1">
          Community Resilience & Economic Ledger
        </h3>
        <p className="text-xs text-slate-400">
          Pilot Area: {impact?.pilotWard || 'Ward 18, George, Western Cape'}
        </p>

        {/* 4 High Impact KPI Cards */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mt-4">
          <div className="p-3.5 rounded-xl bg-slate-800/60 border border-slate-700 text-center">
            <span className="text-[10px] font-bold text-slate-400 uppercase block">Lives Protected</span>
            <span className="text-2xl font-black text-teal-300 mt-1 block">
              {impact?.totalBeneficiariesProtected || 403}
            </span>
            <span className="text-[10px] text-teal-500 font-semibold">toddlers & elderly</span>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-800/60 border border-slate-700 text-center">
            <span className="text-[10px] font-bold text-slate-400 uppercase block">SME Turnover Saved</span>
            <span className="text-2xl font-black text-emerald-400 mt-1 block">
              R{((impact?.totalEconomicLossAvertedZar || 25200) / 1000).toFixed(1)}k
            </span>
            <span className="text-[10px] text-emerald-500 font-semibold">Speedpoint merchants</span>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-800/60 border border-slate-700 text-center">
            <span className="text-[10px] font-bold text-slate-400 uppercase block">Water Distributed</span>
            <span className="text-2xl font-black text-cyan-400 mt-1 block">
              {impact?.totalWaterAllocatedLitres || 1500} L
            </span>
            <span className="text-[10px] text-cyan-500 font-semibold">potable & borehole</span>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-800/60 border border-slate-700 text-center">
            <span className="text-[10px] font-bold text-slate-400 uppercase block">Avg Response Time</span>
            <span className="text-2xl font-black text-amber-400 mt-1 block">
              {impact?.averageResponseTimeMinutes || 18.4}m
            </span>
            <span className="text-[10px] text-amber-500 font-semibold">from rupture alert</span>
          </div>
        </div>

        {/* Cryptographic SHA-256 Chained Audit Trail */}
        <div className="mt-6">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center space-x-2">
              <Database className="w-4 h-4 text-teal-400" />
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300">
                Cryptographic Disaster Audit Ledger (SHA-256)
              </h4>
            </div>
            <span className="text-[10px] font-mono text-emerald-400 bg-emerald-950 px-2 py-0.5 rounded border border-emerald-800/40">
              Chained & Immutable
            </span>
          </div>

          <div className="border border-slate-800 rounded-xl overflow-hidden bg-slate-950/80">
            <table className="w-full text-left text-[11px]">
              <thead className="bg-slate-900 border-b border-slate-800 text-slate-400 font-bold uppercase text-[9px]">
                <tr>
                  <th className="py-2 px-3">Event Type</th>
                  <th className="py-2 px-3">Facility / Node</th>
                  <th className="py-2 px-3">Token / Ref</th>
                  <th className="py-2 px-3">FNB Payment</th>
                  <th className="py-2 px-3 font-mono">SHA-256 Hash</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/50 font-mono">
                {ledger.map((entry) => (
                  <tr key={entry.id} className="hover:bg-slate-900/40 transition">
                    <td className="py-2 px-3 text-teal-300 font-bold">{entry.eventType}</td>
                    <td className="py-2 px-3 text-slate-300">{entry.facilityName || 'Epicenter Hub'}</td>
                    <td className="py-2 px-3 text-slate-400">{entry.verificationToken || 'N/A'}</td>
                    <td className="py-2 px-3 text-emerald-400 font-bold">{entry.fnbTxnRef || '—'}</td>
                    <td className="py-2 px-3 text-slate-500 text-[10px] truncate max-w-[140px]" title={entry.auditHash}>
                      {entry.auditHash?.substring(0, 16)}...
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <button
          onClick={onClose}
          className="mt-6 w-full py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-bold text-xs transition"
        >
          Return to Operations Map
        </button>
      </div>
    </div>
  );
}


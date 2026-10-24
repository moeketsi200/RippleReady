import React from 'react';
import { Activity, ShieldCheck, AlertTriangle, RefreshCw, BarChart3, PlusCircle } from 'lucide-react';

export default function Navbar({
  backendConnected,
  onLoadDemo,
  onOpenReport,
  onOpenImpact,
  loading
}) {
  return (
    <header className="sticky top-0 z-50 bg-[#0B1519]/90 backdrop-blur-md border-b border-teal-950/60 px-4 py-3">
      <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
        {/* Brand */}
        <div className="flex items-center space-x-3">
          <img
            src="/logo.svg"
            alt="RippleReady Logo"
            className="w-10 h-10 rounded-xl shadow-lg shadow-teal-500/30 hover:scale-105 transition-transform duration-200"
          />
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-xl font-bold tracking-tight text-white flex items-center">
                RippleReady
              </h1>
              <span className="text-[10px] font-semibold uppercase px-2 py-0.5 rounded-full bg-teal-500/10 text-teal-400 border border-teal-500/30">
                FNB Hackathon 2026
              </span>
            </div>
            <p className="text-xs text-slate-400 hidden sm:block">
              Solve the problem before it spreads • Ward 18, George
            </p>
          </div>
        </div>

        {/* Status & Actions */}
        <div className="flex items-center space-x-2 sm:space-x-3">
          {/* Connection status */}
          <div className="flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-slate-900/80 border border-slate-800 text-xs">
            <span className={`w-2 h-2 rounded-full ${backendConnected ? 'bg-emerald-400 animate-pulse' : 'bg-amber-400'}`} />
            <span className="text-slate-300 font-medium hidden md:inline">
              {backendConnected ? 'Spring Boot 3.3 Connected' : 'Pitch Demo Mode'}
            </span>
          </div>

          {/* Quick Demo Trigger */}
          <button
            onClick={onLoadDemo}
            disabled={loading}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-teal-500/10 hover:bg-teal-500/20 text-teal-300 border border-teal-500/30 text-xs font-semibold transition"
            title="Load George Ward 18 water main burst scenario"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Demo Scenario</span>
          </button>

          {/* Impact Dashboard */}
          <button
            onClick={onOpenImpact}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-slate-800/80 hover:bg-slate-700/80 text-slate-200 border border-slate-700 text-xs font-semibold transition"
          >
            <BarChart3 className="w-3.5 h-3.5 text-teal-400" />
            <span className="hidden sm:inline">Impact Ledger</span>
          </button>

          {/* Log Incident */}
          <button
            onClick={onOpenReport}
            className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-lg bg-gradient-to-r from-red-600 to-orange-600 hover:from-red-500 hover:to-orange-500 text-white shadow-md shadow-red-600/20 text-xs font-bold transition"
          >
            <PlusCircle className="w-3.5 h-3.5" />
            <span>Report Outage</span>
          </button>
        </div>
      </div>
    </header>
  );
}


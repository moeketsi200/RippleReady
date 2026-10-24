import React, { useState } from 'react';
import { X, AlertOctagon, MapPin, ArrowRight } from 'lucide-react';

export default function IncidentReportModal({
  isOpen,
  onClose,
  onSubmitReport
}) {
  if (!isOpen) return null;

  const [category, setCategory] = useState('Water');
  const [severity, setSeverity] = useState('CRITICAL');
  const [latitude, setLatitude] = useState(-33.9630);
  const [longitude, setLongitude] = useState(22.4610);
  const [ward, setWard] = useState('Ward 18, George');
  const [locationDescription, setLocationDescription] = useState('York Street primary feeder rupture outside Rosemoor');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    await onSubmitReport({
      reporterId: 'rep-george-04',
      category,
      severity,
      latitude: Number(latitude),
      longitude: Number(longitude),
      ward,
      locationDescription
    });
    setSubmitting(false);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-700 rounded-2xl p-6 shadow-2xl text-slate-100">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-2 text-red-400">
          <AlertOctagon className="w-5 h-5" />
          <span className="text-xs font-bold uppercase tracking-wider">Epic 1 • Incident Ingestion</span>
        </div>
        <h3 className="text-lg font-bold text-white mt-1">
          Report Essential Service Disruption
        </h3>
        <p className="text-xs text-slate-400 mt-0.5">
          Establishes the ground-truth epicenter and computes downstream ripple perimeters.
        </p>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Category:</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs text-white"
              >
                <option value="Water">Water Outage</option>
                <option value="Electricity">Electricity Disruption</option>
                <option value="Sanitation">Sanitation Failure</option>
              </select>
            </div>
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Severity:</label>
              <select
                value={severity}
                onChange={(e) => setSeverity(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs text-white"
              >
                <option value="CRITICAL">Critical Main Rupture</option>
                <option value="HIGH">Feeder Pipe Burst</option>
                <option value="MODERATE">Local Valve Leak</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Latitude:</label>
              <input
                type="number"
                step="0.0001"
                value={latitude}
                onChange={(e) => setLatitude(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs font-mono text-white"
                required
              />
            </div>
            <div>
              <label className="text-xs font-bold text-slate-300 block mb-1">Longitude:</label>
              <input
                type="number"
                step="0.0001"
                value={longitude}
                onChange={(e) => setLongitude(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs font-mono text-white"
                required
              />
            </div>
          </div>

          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Ward / Corridor:</label>
            <input
              type="text"
              value={ward}
              onChange={(e) => setWard(e.target.value)}
              className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs text-white"
              required
            />
          </div>

          <div>
            <label className="text-xs font-bold text-slate-300 block mb-1">Location Description:</label>
            <input
              type="text"
              value={locationDescription}
              onChange={(e) => setLocationDescription(e.target.value)}
              className="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-xs text-white"
              required
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full py-2.5 rounded-xl bg-gradient-to-r from-red-600 to-orange-600 hover:from-red-500 hover:to-orange-500 text-white font-extrabold text-sm shadow-lg shadow-red-600/20 transition flex items-center justify-center space-x-2"
          >
            <span>{submitting ? 'Propagating Ripple...' : 'Publish Incident & Propagate Ripple'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
}

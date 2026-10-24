import React, { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Circle, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import { Droplet, AlertTriangle, ShieldCheck, HeartHandshake, Home, Store, School } from 'lucide-react';

const MAP_STYLES = {
  dark: {
    label: '🌙 Dark Ops',
    url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    className: 'dark-tiles',
    maxZoom: 19
  },
  satellite: {
    label: '🛰️ Satellite',
    url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, USGS, GeoEye',
    className: '',
    maxZoom: 18
  },
  streets: {
    label: '🗺️ Streets',
    url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    className: '',
    maxZoom: 19
  }
};

// Controller to handle camera flying and dynamic layout size changes
function MapCameraController({ focusTarget, incident, mapViewMode }) {
  const map = useMap();

  useEffect(() => {
    const timer = setTimeout(() => {
      map.invalidateSize();
    }, 320);
    return () => clearTimeout(timer);
  }, [mapViewMode, map]);

  useEffect(() => {
    if (focusTarget && focusTarget.latitude && focusTarget.longitude) {
      map.flyTo([focusTarget.latitude, focusTarget.longitude], focusTarget.zoom || 17, {
        duration: 1.2
      });
    }
  }, [focusTarget, map]);

  return null;
}

// Controller for Zoom Out (Fit Zone) and Center Epicenter
function MapNavControls({ incident, facilities, resources, onResetEpicenter }) {
  const map = useMap();

  const handleFitZone = () => {
    const pad = 0.022;
    const bounds = [
      [incident.latitude + pad, incident.longitude + pad],
      [incident.latitude - pad, incident.longitude - pad],
      ...facilities.map((f) => [f.latitude, f.longitude]),
      ...resources.map((r) => [r.latitude, r.longitude])
    ];
    map.fitBounds(bounds, { padding: [50, 50], duration: 1.2 });
  };

  const handleCenterEpicenter = () => {
    map.flyTo([incident.latitude, incident.longitude], 15, { duration: 1.2 });
    if (onResetEpicenter) onResetEpicenter();
  };

  return (
    <div className="absolute top-20 left-3 flex flex-col space-y-1.5 z-[400]">
      <button
        type="button"
        onClick={handleFitZone}
        title="Zoom Out: Fit Entire 2.0km Ripple Zone"
        className="flex items-center space-x-1 px-2.5 py-1.5 rounded-lg bg-slate-950/95 hover:bg-slate-800 text-teal-400 hover:text-teal-300 text-xs font-bold border border-slate-700 shadow-xl backdrop-blur-md transition hover:scale-105"
      >
        <span>🔍 Fit Zone</span>
      </button>
      <button
        type="button"
        onClick={handleCenterEpicenter}
        title="Center Outage Epicenter"
        className="flex items-center space-x-1 px-2.5 py-1.5 rounded-lg bg-slate-950/95 hover:bg-slate-800 text-red-400 hover:text-red-300 text-xs font-bold border border-slate-700 shadow-xl backdrop-blur-md transition hover:scale-105"
      >
        <span>🎯 Outage</span>
      </button>
    </div>
  );
}

// Custom DivIcons for Modern Dark UI
const createEpicenterIcon = () => {
  return L.divIcon({
    className: 'custom-epicenter-marker',
    html: `
      <div class="relative flex items-center justify-center w-8 h-8">
        <span class="absolute w-8 h-8 rounded-full bg-red-500/40 animate-ping"></span>
        <span class="relative flex items-center justify-center w-6 h-6 rounded-full bg-red-600 text-white shadow-lg shadow-red-600/50 text-xs font-black">
          !
        </span>
      </div>
    `,
    iconSize: [32, 32],
    iconAnchor: [16, 16]
  });
};

const createFacilityIcon = (facility, isFocused = false) => {
  const isResolved = facility.status === 'RESOLVED';
  const isDispatched = facility.status === 'DISPATCHED';

  let bgColor = 'bg-amber-500';
  let ringColor = isFocused ? 'border-teal-300 ring-4 ring-teal-400/60 scale-125' : 'border-amber-400';
  let badgeText = 'THREAT';

  if (isResolved) {
    bgColor = 'bg-emerald-500';
    ringColor = isFocused ? 'border-teal-300 ring-4 ring-teal-400/60 scale-125' : 'border-emerald-300';
    badgeText = 'RESOLVED';
  } else if (isDispatched) {
    bgColor = 'bg-cyan-500';
    ringColor = isFocused ? 'border-teal-300 ring-4 ring-teal-400/60 scale-125' : 'border-cyan-300';
    badgeText = 'IN TRANSIT';
  } else if (facility.urgencyLevel === 'CRITICAL') {
    bgColor = 'bg-red-500';
    ringColor = isFocused ? 'border-teal-300 ring-4 ring-teal-400/60 scale-125' : 'border-red-400';
    badgeText = 'CRITICAL';
  }

  return L.divIcon({
    className: 'custom-facility-marker',
    html: `
      <div class="relative flex flex-col items-center">
        <div class="flex items-center justify-center w-8 h-8 rounded-xl ${bgColor} text-white shadow-md border-2 ${ringColor} text-xs font-bold transition transform hover:scale-110">
          ${facility.type === 'CRECHE' ? '👶' : facility.type === 'ELDERLY_CARE' ? '👵' : facility.type === 'SOUP_KITCHEN' ? '🍲' : '🏪'}
        </div>
        <span class="mt-1 px-1.5 py-0.2 text-[9px] font-extrabold uppercase rounded bg-slate-900/90 text-white border border-slate-700 whitespace-nowrap">
          ${facility.name.split(' ')[0]}
        </span>
      </div>
    `,
    iconSize: [40, 48],
    iconAnchor: [20, 24]
  });
};

const createResourceIcon = (resource) => {
  return L.divIcon({
    className: 'custom-resource-marker',
    html: `
      <div class="relative flex flex-col items-center">
        <div class="flex items-center justify-center w-8 h-8 rounded-full bg-teal-500 text-white shadow-lg shadow-teal-500/30 border-2 border-teal-200 text-xs font-bold transition transform hover:scale-110">
          💧
        </div>
        <span class="mt-1 px-1.5 py-0.2 text-[9px] font-bold rounded bg-teal-950/90 text-teal-300 border border-teal-700/50 whitespace-nowrap">
          ${resource.availableCapacity}L
        </span>
      </div>
    `,
    iconSize: [40, 48],
    iconAnchor: [20, 24]
  });
};

export default function RippleMap({
  incident,
  facilities,
  resources,
  focusTarget,
  mapViewMode = 'split',
  onToggleViewMode,
  onResetEpicenter,
  onSelectFacility,
  onSelectResource
}) {
  const [mapStyle, setMapStyle] = useState('dark');
  const mapCenter = [incident.latitude, incident.longitude];
  const currentTile = MAP_STYLES[mapStyle] || MAP_STYLES.dark;

  const heightClass =
    mapViewMode === 'minimized'
      ? 'h-[250px] lg:h-[280px]'
      : mapViewMode === 'maximized'
      ? 'h-[calc(100vh-140px)]'
      : 'h-[60vh] lg:h-[calc(100vh-140px)]';

  return (
    <div className={`relative w-full ${heightClass} rounded-2xl overflow-hidden border border-slate-800 shadow-2xl bg-slate-950 transition-all duration-300`}>
      <MapContainer
        center={mapCenter}
        zoom={15}
        scrollWheelZoom={true}
        className="w-full h-full"
      >
        <MapCameraController
          focusTarget={focusTarget}
          incident={incident}
          mapViewMode={mapViewMode}
        />
        <MapNavControls
          incident={incident}
          facilities={facilities}
          resources={resources}
          onResetEpicenter={onResetEpicenter}
        />

        {/* Clean, Watermark-Free TileLayer (No API Key Required) */}
        <TileLayer
          key={mapStyle}
          attribution={currentTile.attribution}
          url={currentTile.url}
          className={currentTile.className}
          maxZoom={currentTile.maxZoom}
        />

        {/* Outage Epicenter & Ripple Perimeter Circle */}
        <Circle
          center={mapCenter}
          radius={(incident.impactRadiusKm || 2.0) * 1000}
          pathOptions={{
            color: '#EF4444',
            fillColor: '#EF4444',
            fillOpacity: 0.12,
            weight: 2,
            dashArray: '4, 8'
          }}
        />

        <Marker position={mapCenter} icon={createEpicenterIcon()}>
          <Popup className="custom-popup">
            <div className="p-2 text-slate-900">
              <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-700 uppercase">
                Epicenter ({incident.category})
              </span>
              <h4 className="font-bold text-sm mt-1">{incident.wardNumber} Disruption</h4>
              <p className="text-xs text-slate-600 mt-1">{incident.locationDescription}</p>
              <div className="mt-2 text-[11px] font-semibold text-slate-700">
                Radius: {incident.impactRadiusKm} km • Confidence: {incident.confidenceScore}%
              </div>
            </div>
          </Popup>
        </Marker>

        {/* Threatened / Downstream Dependent Facilities */}
        {facilities.map((fac) => {
          const isFocused = focusTarget?.id === fac.id;
          return (
            <Marker
              key={fac.id}
              position={[fac.latitude, fac.longitude]}
              icon={createFacilityIcon(fac, isFocused)}
              eventHandlers={{
                click: () => onSelectFacility(fac)
              }}
            >
              <Popup>
                <div className="p-2 text-slate-900 min-w-[200px]">
                  <div className="flex items-center justify-between">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                      fac.status === 'RESOLVED' ? 'bg-emerald-100 text-emerald-700' :
                      fac.status === 'DISPATCHED' ? 'bg-cyan-100 text-cyan-700' : 'bg-red-100 text-red-700'
                    }`}>
                      {fac.status}
                    </span>
                    <span className="text-[10px] font-bold text-slate-500">
                      Tier {fac.priorityTier || 1}
                    </span>
                  </div>
                  <h4 className="font-bold text-sm mt-1">{fac.name}</h4>
                  <p className="text-xs text-slate-600 mt-0.5">{fac.primaryNeed}</p>
                  <div className="mt-2 pt-2 border-t border-slate-200 text-xs flex justify-between font-medium">
                    <span>Buffer: <b>{fac.bufferHours}h remaining</b></span>
                    <span>Impact: <b>{fac.beneficiaryCount} people</b></span>
                  </div>
                  <button
                    onClick={() => onSelectFacility(fac)}
                    className="mt-2 w-full py-1 rounded bg-teal-600 hover:bg-teal-700 text-white text-xs font-bold transition"
                  >
                    {fac.status === 'RESOLVED' ? 'View Proof of Delivery' : 'Coordinate Mutual Aid'}
                  </button>
                </div>
              </Popup>
            </Marker>
          );
        })}

        {/* Registered Mutual Aid Reserves (Boreholes, JoJo Tanks) */}
        {resources.map((res) => (
          <Marker
            key={res.id}
            position={[res.latitude, res.longitude]}
            icon={createResourceIcon(res)}
            eventHandlers={{
              click: () => onSelectResource(res)
            }}
          >
            <Popup>
              <div className="p-2 text-slate-900 min-w-[210px]">
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-teal-100 text-teal-800 uppercase">
                  Community Reserve
                </span>
                <h4 className="font-bold text-sm mt-1">{res.providerName}</h4>
                <p className="text-xs text-slate-600">{res.resourceTypeDisplay || res.resourceType}</p>
                <div className="mt-2 pt-2 border-t border-slate-200 text-xs flex justify-between font-bold text-teal-800">
                  <span>Available:</span>
                  <span>{res.availableCapacity} / {res.totalCapacity} {res.unitMeasure}</span>
                </div>
                <button
                  onClick={() => onSelectResource(res)}
                  className="mt-2 w-full py-1 rounded bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold transition"
                >
                  Pledge This Capacity
                </button>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>

      {/* Top Controls: Basemap & View Mode Switcher */}
      <div className="absolute top-4 right-4 z-[400] flex items-center space-x-2">
        {/* Basemap Style Switcher */}
        <div className="flex bg-slate-950/90 backdrop-blur-md rounded-xl p-1 border border-slate-800 shadow-xl gap-1">
          {Object.entries(MAP_STYLES).map(([key, style]) => (
            <button
              key={key}
              type="button"
              onClick={() => setMapStyle(key)}
              className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition ${
                mapStyle === key
                  ? 'bg-teal-500 text-slate-950 font-bold shadow'
                  : 'text-slate-400 hover:text-white hover:bg-slate-850'
              }`}
            >
              {style.label}
            </button>
          ))}
        </div>

        {/* View Mode Switcher (Min / Split / Max) */}
        {onToggleViewMode && (
          <div className="flex bg-slate-950/90 backdrop-blur-md rounded-xl p-1 border border-slate-800 shadow-xl gap-1">
            <button
              type="button"
              onClick={() => onToggleViewMode(mapViewMode === 'minimized' ? 'split' : 'minimized')}
              title={mapViewMode === 'minimized' ? 'Restore Split View' : 'Minimize Map (Focus on Ledger)'}
              className={`px-2.5 py-1 rounded-lg text-xs font-bold transition ${
                mapViewMode === 'minimized'
                  ? 'bg-teal-500 text-slate-950 font-black shadow'
                  : 'text-slate-400 hover:text-white hover:bg-slate-850'
              }`}
            >
              {mapViewMode === 'minimized' ? '⚖️ Restore' : '🗕 Min Map'}
            </button>
            <button
              type="button"
              onClick={() => onToggleViewMode(mapViewMode === 'maximized' ? 'split' : 'maximized')}
              title={mapViewMode === 'maximized' ? 'Restore Split View' : 'Maximize Map Full Screen'}
              className={`px-2.5 py-1 rounded-lg text-xs font-bold transition ${
                mapViewMode === 'maximized'
                  ? 'bg-teal-500 text-slate-950 font-black shadow'
                  : 'text-slate-400 hover:text-white hover:bg-slate-850'
              }`}
            >
              {mapViewMode === 'maximized' ? '⚖️ Split' : '⛶ Max Map'}
            </button>
          </div>
        )}
      </div>

      {/* Floating Legend Overlay */}
      <div className="absolute bottom-4 left-4 z-[400] bg-slate-950/90 backdrop-blur-md border border-slate-800 rounded-xl p-3 text-xs shadow-xl flex flex-col space-y-1.5 text-slate-300">
        <span className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider">Map Legend</span>
        <div className="flex items-center space-x-2">
          <span className="w-3 h-3 rounded-full bg-red-500 animate-ping" />
          <span>Outage Epicenter (Ward 18)</span>
        </div>
        <div className="flex items-center space-x-2">
          <span className="w-3 h-3 rounded-full bg-amber-500" />
          <span>Threatened Facility (&le; 2h buffer)</span>
        </div>
        <div className="flex items-center space-x-2">
          <span className="w-3 h-3 rounded-full bg-teal-500" />
          <span>Available Borehole / JoJo Reserve</span>
        </div>
        <div className="flex items-center space-x-2">
          <span className="w-3 h-3 rounded-full bg-emerald-500" />
          <span>Resolved / Mutual Aid Delivered</span>
        </div>
      </div>
    </div>
  );
}


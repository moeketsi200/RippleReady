import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import RippleMap from './components/RippleMap';
import CascadeSummaryPanel from './components/CascadeSummaryPanel';
import MutualAidPledgeModal from './components/MutualAidPledgeModal';
import DeliveryHandshakeModal from './components/DeliveryHandshakeModal';
import IncidentReportModal from './components/IncidentReportModal';
import ImpactDashboardModal from './components/ImpactDashboardModal';
import { api } from './services/api';
import { DEFAULT_INCIDENT, INITIAL_FACILITIES, INITIAL_RESOURCES } from './services/mockData';

export default function App() {
  const [incident, setIncident] = useState(DEFAULT_INCIDENT);
  const [facilities, setFacilities] = useState(INITIAL_FACILITIES);
  const [resources, setResources] = useState(INITIAL_RESOURCES);
  const [simulationResult, setSimulationResult] = useState(null);

  // Modals & Selections
  const [selectedFacility, setSelectedFacility] = useState(null);
  const [selectedResource, setSelectedResource] = useState(null);
  const [isPledgeOpen, setIsPledgeOpen] = useState(false);
  const [isVerifyOpen, setIsVerifyOpen] = useState(false);
  const [isReportOpen, setIsReportOpen] = useState(false);
  const [isImpactOpen, setIsImpactOpen] = useState(false);

  const [backendConnected, setBackendConnected] = useState(false);
  const [loading, setLoading] = useState(false);

  // Initial check & cascade calculation
  useEffect(() => {
    async function init() {
      const health = await api.checkHealth();
      setBackendConnected(health.connected);

      const sim = await api.simulateCascade(DEFAULT_INCIDENT);
      setSimulationResult(sim);
    }
    init();
  }, []);

  // Demo Scenario Handler (George Ward 18 Burst)
  const handleLoadDemo = async () => {
    setLoading(true);
    const demo = await api.getDemoScenario();
    setIncident(demo);
    setFacilities(INITIAL_FACILITIES);
    setResources(INITIAL_RESOURCES);

    const sim = await api.simulateCascade(demo);
    setSimulationResult(sim);
    setLoading(false);
  };

  // Submit Disruption Incident Report
  const handleSubmitReport = async (reportData) => {
    setLoading(true);
    const created = await api.submitIncident(reportData);
    setIncident({
      ...incident,
      ...created,
      impactRadiusKm: 2.0
    });

    const sim = await api.simulateCascade({
      category: created.category.toUpperCase(),
      severity: created.severity || "CRITICAL_MAIN_RUPTURE",
      latitude: created.latitude,
      longitude: created.longitude,
      wardNumber: created.ward,
      estimatedOutageDurationHours: 6.0,
      impactRadiusKm: 2.0,
      sourcingRadiusKm: 3.5
    });
    setSimulationResult(sim);
    setLoading(false);
  };

  // Pledge Mutual Aid Capacity (Epic 3)
  const handleConfirmPledge = async (pledgeData) => {
    const pledgeRes = await api.createPledge(pledgeData);

    // Update facility status to DISPATCHED
    setFacilities((prev) =>
      prev.map((f) => (f.id === pledgeData.facilityId ? { ...f, status: 'DISPATCHED' } : f))
    );

    // Decrement resource capacity
    setResources((prev) =>
      prev.map((r) =>
        r.id === pledgeData.resourceId
          ? { ...r, availableCapacity: Math.max(0, r.availableCapacity - pledgeData.allocatedUnits) }
          : r
      )
    );
  };

  // Verify Delivery Handshake & Authorize FNB Payout (Epic 4 & 5)
  const handleVerifyHandshake = async (verificationData) => {
    const res = await api.verifyDelivery(verificationData);

    if (res && res.facilityId) {
      // Transition facility status to RESOLVED (turns green!)
      setFacilities((prev) =>
        prev.map((f) => (f.id === res.facilityId ? { ...f, status: 'RESOLVED' } : f))
      );
    }
    return res;
  };

  return (
    <div className="min-h-screen bg-[#071013] text-slate-100 flex flex-col font-['Plus_Jakarta_Sans',sans-serif]">
      {/* Top Navbar */}
      <Navbar
        backendConnected={backendConnected}
        onLoadDemo={handleLoadDemo}
        onOpenReport={() => setIsReportOpen(true)}
        onOpenImpact={() => setIsImpactOpen(true)}
        loading={loading}
      />

      {/* Main Operations Console */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-3 sm:p-4 grid grid-cols-1 lg:grid-cols-12 gap-4">
        {/* Left Column: Interactive Map (8 cols on desktop) */}
        <section className="lg:col-span-8 flex flex-col">
          <RippleMap
            incident={incident}
            facilities={facilities}
            resources={resources}
            onSelectFacility={(fac) => {
              setSelectedFacility(fac);
              if (fac.status === 'DISPATCHED') {
                setIsVerifyOpen(true);
              } else {
                setIsPledgeOpen(true);
              }
            }}
            onSelectResource={(res) => {
              setSelectedResource(res);
              setIsPledgeOpen(true);
            }}
          />
        </section>

        {/* Right Column: Cascading Interdependency Ledger (4 cols on desktop) */}
        <section className="lg:col-span-4 flex flex-col h-[600px] lg:h-[calc(100vh-140px)]">
          <CascadeSummaryPanel
            facilities={facilities}
            simulationResult={simulationResult}
            onOpenPledge={(fac) => {
              setSelectedFacility(fac);
              setIsPledgeOpen(true);
            }}
            onOpenVerify={(fac) => {
              setSelectedFacility(fac);
              setIsVerifyOpen(true);
            }}
          />
        </section>
      </main>

      {/* Modals */}
      <MutualAidPledgeModal
        facility={selectedFacility}
        resources={resources}
        isOpen={isPledgeOpen}
        onClose={() => setIsPledgeOpen(false)}
        onConfirmPledge={handleConfirmPledge}
      />

      <DeliveryHandshakeModal
        facility={selectedFacility}
        isOpen={isVerifyOpen}
        onClose={() => setIsVerifyOpen(false)}
        onVerifyHandshake={handleVerifyHandshake}
      />

      <IncidentReportModal
        isOpen={isReportOpen}
        onClose={() => setIsReportOpen(false)}
        onSubmitReport={handleSubmitReport}
      />

      <ImpactDashboardModal
        isOpen={isImpactOpen}
        onClose={() => setIsImpactOpen(false)}
      />
    </div>
  );
}


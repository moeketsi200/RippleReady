import { DEFAULT_INCIDENT, INITIAL_FACILITIES, INITIAL_RESOURCES } from './mockData';

const BASE_URL = '/api';

export const api = {
  // Check backend health
  async checkHealth() {
    try {
      const res = await fetch(`${BASE_URL.replace('/api', '')}/healthz`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) {
        const data = await res.json();
        return { connected: true, data };
      }
    } catch (e) {
      // Backend not running
    }
    return { connected: false, message: 'Running in Local Pitch Simulation Mode' };
  },

  // Get demo scenario
  async getDemoScenario() {
    try {
      const res = await fetch(`${BASE_URL}/ripple/demo-scenario`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch (e) {}
    return DEFAULT_INCIDENT;
  },

  // Simulate Ripple Cascade
  async simulateCascade(request) {
    try {
      const res = await fetch(`${BASE_URL}/ripple/simulate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
        signal: AbortSignal.timeout(2000)
      });
      if (res.ok) return await res.json();
    } catch (e) {}

    // Fallback simulation calculation
    return {
      outageRequest: request,
      totalSecondaryRisks: INITIAL_FACILITIES.length,
      criticalRiskCount: INITIAL_FACILITIES.filter(f => f.urgencyLevel === 'CRITICAL').length,
      highRiskCount: INITIAL_FACILITIES.filter(f => f.urgencyLevel === 'HIGH').length,
      moderateRiskCount: INITIAL_FACILITIES.filter(f => f.urgencyLevel === 'MODERATE').length,
      totalBeneficiariesThreatened: INITIAL_FACILITIES.reduce((acc, f) => acc + f.beneficiaryCount, 0),
      totalProjectedEconomicLossZar: 25200.0,
      threatenedFacilities: INITIAL_FACILITIES.map(f => ({
        facility: f,
        distanceKm: f.distanceKm,
        urgencyLevel: f.urgencyLevel,
        estimatedRunwayMinutes: f.bufferHours * 60,
        projectedEconomicLossZar: f.estimatedDailyLossZar * 0.75,
        cascadeRiskSummary: `Buffer expires in ${f.bufferHours * 60} mins: ${f.beneficiaryCount} patrons/beneficiaries vulnerable.`
      })),
      executionTimeMs: 2,
      generatedAt: new Date().toISOString()
    };
  },

  // Ingest Incident Report
  async submitIncident(request) {
    try {
      const res = await fetch(`${BASE_URL}/incidents`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
        signal: AbortSignal.timeout(2000)
      });
      if (res.ok) return await res.json();
    } catch (e) {}

    return {
      id: "inc-001",
      reporterId: request.reporterId || "rep-george-04",
      category: request.category || "Water",
      severity: request.severity || "CRITICAL",
      latitude: request.latitude,
      longitude: request.longitude,
      ward: request.ward || "Ward 18, George",
      status: "VERIFIED",
      impactRadiusKm: 2.0,
      confidenceScore: 100,
      isDuplicate: false
    };
  },

  // Match mutual aid resources
  async findMatchingResources(lat, lon, radiusKm = 3.0, requiredVolume = 500.0) {
    try {
      const res = await fetch(`${BASE_URL}/resources/match?lat=${lat}&lon=${lon}&radiusKm=${radiusKm}&requiredVolume=${requiredVolume}`, { signal: AbortSignal.timeout(2000) });
      if (res.ok) return await res.json();
    } catch (e) {}

    return INITIAL_RESOURCES.map(r => ({
      resource: r,
      distanceKm: r.distanceKm,
      estimatedArrivalMinutes: Math.round(8 + r.distanceKm * 2.5),
      suggestedAllocationUnits: requiredVolume,
      matchedFacilityId: null,
      matchedFacilityName: null,
      status: "AVAILABLE"
    }));
  },

  // Pledge mutual aid capacity
  async createPledge(request) {
    try {
      const res = await fetch(`${BASE_URL}/resources/pledge`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
        signal: AbortSignal.timeout(2000)
      });
      if (res.ok) return await res.json();
    } catch (e) {}

    const token = request.verificationToken || "TOKEN-" + Math.floor(100 + Math.random() * 899);
    return {
      id: "pld-" + Math.random().toString(36).substring(2, 9),
      resourceId: request.resourceId,
      resourceProviderName: "Hendricks Hardware & Agri Supplies",
      facilityId: request.facilityId,
      facilityName: "Little Stars Crèche",
      allocatedUnits: request.allocatedUnits,
      unitMeasure: "Litres",
      pledgerContact: request.pledgerContact,
      notes: request.notes,
      status: "DISPATCHED",
      verificationToken: token,
      pledgedAt: new Date().toISOString()
    };
  },

  // Confirm delivery handshake & trigger FNB Instant Pay
  async verifyDelivery(request) {
    try {
      const res = await fetch(`${BASE_URL}/deliveries/verify`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
        signal: AbortSignal.timeout(2000)
      });
      if (res.ok) return await res.json();
    } catch (e) {}

    return {
      pledgeId: "pld-verified-01",
      facilityId: "fac-1",
      facilityName: "Little Stars Crèche",
      facilityStatus: "RESOLVED",
      dashboardNodeColor: "GREEN",
      litersDelivered: 500.0,
      beneficiariesProtected: 40,
      fnbDisbursement: {
        transactionReference: "FNB-PAY-" + Math.random().toString(36).substring(2, 10).toUpperCase(),
        recipientMobile: "+27 82 555 9901",
        amountZar: 150.0,
        paymentRail: "FNB Instant Pay / eWallet Rail",
        purpose: "Fuel Stipend - 4.0 km round trip for 500L emergency water delivery",
        status: "SUCCESS",
        disbursedAt: new Date().toISOString()
      },
      verifiedAt: new Date().toISOString()
    };
  },

  // Impact summary
  async getImpactSummary() {
    try {
      const res = await fetch(`${BASE_URL}/impact/summary`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch (e) {}

    return {
      totalIncidentsIntercepted: 1,
      secondaryCrisesAverted: 5,
      totalBeneficiariesProtected: 403,
      totalEconomicLossAvertedZar: 25200.0,
      totalWaterAllocatedLitres: 1500.0,
      activeMutualAidPledges: 3,
      averageResponseTimeMinutes: 18.4,
      pilotWard: "Ward 18, George, Western Cape"
    };
  },

  // Audit ledger
  async getAuditLedger() {
    try {
      const res = await fetch(`${BASE_URL}/audit/ledger`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch (e) {}

    return [
      {
        id: "audit-01",
        timestamp: new Date().toISOString(),
        eventType: "INCIDENT_REPORTED",
        facilityId: null,
        facilityName: null,
        resourceId: null,
        verificationToken: "INC-WARD18",
        litersDelivered: 0,
        beneficiariesProtected: 403,
        fnbTxnRef: null,
        status: "VERIFIED",
        auditHash: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
      },
      {
        id: "audit-02",
        timestamp: new Date().toISOString(),
        eventType: "PLEDGE_RESERVATION_LOCKED",
        facilityId: "fac-1",
        facilityName: "Little Stars Crèche",
        resourceId: "res-1",
        verificationToken: "TOKEN-789",
        litersDelivered: 500.0,
        beneficiariesProtected: 40,
        fnbTxnRef: null,
        status: "DISPATCHED",
        auditHash: "7b8e5c8e4a9e2247b96874e4a7d65b16f38ef8a74e548d94e1e3b5e7d9a1f2c3"
      },
      {
        id: "audit-03",
        timestamp: new Date().toISOString(),
        eventType: "FNB_MICRO_GRANT_DISBURSED",
        facilityId: "fac-1",
        facilityName: "Little Stars Crèche",
        resourceId: "res-1",
        verificationToken: "TOKEN-789",
        litersDelivered: 500.0,
        beneficiariesProtected: 40,
        fnbTxnRef: "FNB-PAY-88B19C",
        status: "RESOLVED",
        auditHash: "a94a8fe5ccb19ba61c4c0873d391e987982fbbd34a49fe05ce3818e6973e2752"
      }
    ];
  }
};

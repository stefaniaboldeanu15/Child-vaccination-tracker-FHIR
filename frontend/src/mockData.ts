export interface Vaccination {
  id: string;
  vaccineName: string;
  vaccineType: string;
  /** Optional standardized coding (preferred for matching/reminders). */
  vaccineSystem?: string;
  /** Optional standardized coding (preferred for matching/reminders). */
  vaccineCode?: string;
  date: string;
  doseNumber: number;
  totalDoses: number;
  manufacturer: string;
  batchNumber: string;
  administeredBy: string;
  location: string;
  status: 'completed' | 'scheduled';
  nextDoseDate?: string;
}

export interface Patient {
  patientId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  country: string;
  vaccinations: Vaccination[];
}

export const mockPatientData: Patient = {
  patientId: 'EU-FR-2024-001234',
  firstName: 'Marie',
  lastName: 'Dubois',
  dateOfBirth: '1985-06-15',
  country: 'France',
  vaccinations: [
    {
      id: 'v1',
      vaccineName: 'Comirnaty',
      vaccineType: 'COVID-19 mRNA',
      date: '2024-09-15',
      doseNumber: 2,
      totalDoses: 2,
      manufacturer: 'Pfizer-BioNTech',
      batchNumber: 'FF2587',
      administeredBy: 'Jean Martin',
      location: 'Hôpital Saint-Antoine, Paris',
      status: 'completed',
    },
    {
      id: 'v2',
      vaccineName: 'Fluenz Tetra',
      vaccineType: 'Influenza',
      date: '2024-10-20',
      doseNumber: 1,
      totalDoses: 1,
      manufacturer: 'AstraZeneca',
      batchNumber: 'FLU2024-789',
      administeredBy: 'Sophie Leroux',
      location: 'Centre de Vaccination, Lyon',
      status: 'completed',
    },
    {
      id: 'v3',
      vaccineName: 'Tetanus-Diphtheria',
      vaccineType: 'Tetanus',
      date: '2023-05-10',
      doseNumber: 1,
      totalDoses: 1,
      manufacturer: 'Sanofi Pasteur',
      batchNumber: 'TD2023-456',
      administeredBy: 'Pierre Rousseau',
      location: 'Clinique du Parc, Nice',
      status: 'completed',
      nextDoseDate: '2033-05-10',
    },
    {
      id: 'v4',
      vaccineName: 'Hepatitis B Vaccine',
      vaccineType: 'Hepatitis B',
      date: '2025-01-15',
      doseNumber: 3,
      totalDoses: 3,
      manufacturer: 'GlaxoSmithKline',
      batchNumber: 'HB2025-123',
      administeredBy: 'Dr. Claire Moreau',
      location: 'Hôpital Saint-Antoine, Paris',
      status: 'scheduled',
    },
  ],
}

export const mockPatientsDatabase: Patient[] = [
  mockPatientData,
  {
    patientId: 'EU-DE-2024-005678',
    firstName: 'Hans',
    lastName: 'Mueller',
    dateOfBirth: '1978-03-22',
    country: 'Germany',
    vaccinations: [
      {
        id: 'v5',
        vaccineName: 'Spikevax',
        vaccineType: 'COVID-19 mRNA',
        date: '2024-08-10',
        doseNumber: 1,
        totalDoses: 2,
        manufacturer: 'Moderna',
        batchNumber: 'MOD2024-111',
        administeredBy: 'Schmidt',
        location: 'Charité Hospital, Berlin',
        status: 'completed',
        nextDoseDate: '2024-12-10',
      },
      {
        id: 'v6',
        vaccineName: 'Prevenar 13',
        vaccineType: 'Pneumococcal',
        date: '2024-11-05',
        doseNumber: 1,
        totalDoses: 1,
        manufacturer: 'Pfizer',
        batchNumber: 'PN2024-333',
        administeredBy: 'Fischer',
        location: 'Munich Medical Center',
        status: 'completed',
      },
    ],
  },
  {
    patientId: 'EU-IT-2024-009876',
    firstName: 'Giulia',
    lastName: 'Rossi',
    dateOfBirth: '1992-11-08',
    country: 'Italy',
    vaccinations: [
      {
        id: 'v7',
        vaccineName: 'Vaxzevria',
        vaccineType: 'COVID-19 Viral Vector',
        date: '2024-07-15',
        doseNumber: 2,
        totalDoses: 2,
        manufacturer: 'AstraZeneca',
        batchNumber: 'AZ2024-555',
        administeredBy: 'Romano',
        location: 'Ospedale San Raffaele, Milano',
        status: 'completed',
      },
      {
        id: 'v8',
        vaccineName: 'Gardasil 9',
        vaccineType: 'HPV',
        date: '2025-02-01',
        doseNumber: 2,
        totalDoses: 3,
        manufacturer: 'Merck',
        batchNumber: 'HPV2025-777',
        administeredBy: 'Dr. Bianchi',
        location: 'Centro Vaccinale Roma',
        status: 'scheduled',
      },
    ],
  },
  {
    patientId: 'EU-ES-2024-012345',
    firstName: 'Carlos',
    lastName: 'García',
    dateOfBirth: '1965-09-30',
    country: 'Spain',
    vaccinations: [
      {
        id: 'v9',
        vaccineName: 'Comirnaty',
        vaccineType: 'COVID-19 mRNA',
        date: '2024-10-01',
        doseNumber: 3,
        totalDoses: 3,
        manufacturer: 'Pfizer-BioNTech',
        batchNumber: 'PF2024-999',
        administeredBy: 'Fernández',
        location: 'Hospital La Paz, Madrid',
        status: 'completed',
      },
      {
        id: 'v10',
        vaccineName: 'MMR II',
        vaccineType: 'MMR (Measles, Mumps, Rubella)',
        date: '2023-04-20',
        doseNumber: 1,
        totalDoses: 1,
        manufacturer: 'Merck',
        batchNumber: 'MMR2023-222',
        administeredBy: 'López',
        location: 'Centro de Salud Barcelona',
        status: 'completed',
      },
      {
        id: 'v11',
        vaccineName: 'Influenza Vaccine',
        vaccineType: 'Influenza',
        date: '2024-12-10',
        doseNumber: 1,
        totalDoses: 1,
        manufacturer: 'Sanofi',
        batchNumber: 'FLU2024-444',
        administeredBy: 'Dr. Martinez',
        location: 'Hospital La Paz, Madrid',
        status: 'scheduled',
      },
    ],
  },
  {
    patientId: 'EU-NL-2024-067890',
    firstName: 'Emma',
    lastName: 'van den Berg',
    dateOfBirth: '2000-01-25',
    country: 'Netherlands',
    vaccinations: [
      {
        id: 'v12',
        vaccineName: 'Comirnaty',
        vaccineType: 'COVID-19 mRNA',
        date: '2024-06-15',
        doseNumber: 2,
        totalDoses: 2,
        manufacturer: 'Pfizer-BioNTech',
        batchNumber: 'PF2024-666',
        administeredBy: 'de Vries',
        location: 'Amsterdam UMC',
        status: 'completed',
      },
    ],
  },
]

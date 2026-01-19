import React, { createContext, useContext, useState, ReactNode } from 'react';

type Language = 'en' | 'hi' | 'mr';

interface Translations {
  [key: string]: {
    en: string;
    hi: string;
    mr: string;
  };
}

const translations: Translations = {
  // Auth
  login: { en: 'Login', hi: 'लॉगिन', mr: 'लॉगिन' },
  loginWithOtp: { en: 'Login with OTP', hi: 'OTP से लॉगिन', mr: 'OTP सह लॉगिन' },
  register: { en: 'Register', hi: 'रजिस्टर', mr: 'नोंदणी' },
  forgotPassword: { en: 'Forgot Password?', hi: 'पासवर्ड भूल गए?', mr: 'पासवर्ड विसरलात?' },
  mobileNumber: { en: 'Mobile Number', hi: 'मोबाइल नंबर', mr: 'मोबाईल नंबर' },
  password: { en: 'Password', hi: 'पासवर्ड', mr: 'पासवर्ड' },
  confirmPassword: { en: 'Confirm Password', hi: 'पासवर्ड की पुष्टि', mr: 'पासवर्ड पुष्टी करा' },
  name: { en: 'Name', hi: 'नाम', mr: 'नाव' },

  // Password Reset
  backToLogin: { en: 'Back to Login', hi: 'लॉगिन पर वापस जाएं', mr: 'लॉगिनवर परत जा' },
  enterMobileForOtp: { en: 'Enter your mobile number, we will send you an OTP', hi: 'अपना मोबाइल नंबर दर्ज करें, हम आपको OTP भेजेंगे', mr: 'तुमचा मोबाईल नंबर टाका, आम्ही तुम्हाला OTP पाठवू' },
  sendOtp: { en: 'Send OTP', hi: 'OTP भेजें', mr: 'OTP पाठवा' },
  sending: { en: 'Sending...', hi: 'भेज रहा है...', mr: 'पाठवत आहे...' },
  otpSent: { en: 'OTP Sent!', hi: 'OTP भेजा गया!', mr: 'OTP पाठवला!' },
  checkPhone: { en: 'Please check your phone', hi: 'कृपया अपना फोन चेक करें', mr: 'कृपया तुमचा फोन तपासा' },
  otpSendFailed: { en: 'Failed to send OTP', hi: 'OTP भेजने में विफल', mr: 'OTP पाठवण्यात अयशस्वी' },
  enterValidMobile: { en: 'Please enter valid 10-digit mobile number', hi: 'कृपया 10 अंकों का मोबाइल नंबर दर्ज करें', mr: 'कृपया 10 अंकी मोबाईल नंबर टाका' },
  enterOtp: { en: 'Enter OTP', hi: 'OTP दर्ज करें', mr: 'OTP टाका' },
  otpSentTo: { en: 'Enter the OTP sent to', hi: 'पर भेजा गया OTP दर्ज करें', mr: 'वर पाठवलेला OTP टाका' },
  verifyOtp: { en: 'Verify OTP', hi: 'OTP सत्यापित करें', mr: 'OTP सत्यापित करा' },
  verifying: { en: 'Verifying...', hi: 'सत्यापित कर रहा है...', mr: 'सत्यापित करत आहे...' },
  otpVerified: { en: 'OTP Verified!', hi: 'OTP सत्यापित!', mr: 'OTP सत्यापित!' },
  setNewPassword: { en: 'Now set a new password', hi: 'अब नया पासवर्ड सेट करें', mr: 'आता नवीन पासवर्ड सेट करा' },
  wrongOtp: { en: 'Wrong OTP', hi: 'गलत OTP', mr: 'चुकीचा OTP' },
  enterNewPassword: { en: 'Enter your new password', hi: 'अपना नया पासवर्ड दर्ज करें', mr: 'तुमचा नवीन पासवर्ड टाका' },
  newPassword: { en: 'New Password', hi: 'नया पासवर्ड', mr: 'नवीन पासवर्ड' },
  minChars: { en: 'Min 8 chars (A-Z, a-z, 0-9, @#$)', hi: 'न्यूनतम 8 अक्षर (A-Z, a-z, 0-9, @#$)', mr: 'किमान 8 अक्षरे (A-Z, a-z, 0-9, @#$)' },
  reenterPassword: { en: 'Re-enter password', hi: 'पासवर्ड दोबारा दर्ज करें', mr: 'पुन्हा पासवर्ड टाका' },
  changePassword: { en: 'Change Password', hi: 'पासवर्ड बदलें', mr: 'पासवर्ड बदला' },
  changing: { en: 'Changing...', hi: 'बदल रहा है...', mr: 'बदलत आहे...' },
  passwordChanged: { en: 'Password changed successfully!', hi: 'पासवर्ड सफलतापूर्वक बदला गया!', mr: 'पासवर्ड यशस्वीरित्या बदलला!' },
  nowLogin: { en: 'Now login', hi: 'अब लॉगिन करें', mr: 'आता लॉगिन करा' },
  passwordChangeFailed: { en: 'Failed to change password', hi: 'पासवर्ड बदलने में विफल', mr: 'पासवर्ड बदलण्यात अयशस्वी' },
  passwordMinLength: { en: 'Password must be at least 6 characters', hi: 'पासवर्ड कम से कम 6 अक्षर का होना चाहिए', mr: 'पासवर्ड किमान 6 अक्षरांचा असावा' },
  passwordMismatch: { en: 'Passwords do not match', hi: 'पासवर्ड मेल नहीं खाते', mr: 'पासवर्ड जुळत नाहीत' },
  enterMobileFirst: { en: 'Please enter mobile number first', hi: 'कृपया पहले मोबाइल नंबर दर्ज करें', mr: 'कृपया प्रथम मोबाईल नंबर टाका' },
  goBack: { en: 'Go Back', hi: 'वापस जाएं', mr: 'मागे जा' },
  tryAgain: { en: 'Please try again', hi: 'कृपया पुनः प्रयास करें', mr: 'कृपया पुन्हा प्रयत्न करा' },
  enter6DigitOtp: { en: 'Please enter 6-digit OTP', hi: 'कृपया 6 अंकों का OTP दर्ज करें', mr: 'कृपया 6 अंकी OTP टाका' },
  enterMobileToReceiveOtp: { en: 'Enter your mobile number to receive OTP', hi: 'OTP प्राप्त करने के लिए अपना मोबाइल नंबर दर्ज करें', mr: 'OTP प्राप्त करण्यासाठी तुमचा मोबाईल नंबर टाका' },
  enter6DigitOtpSent: { en: 'Enter the 6-digit OTP sent to your mobile', hi: 'अपने मोबाइल पर भेजा गया 6 अंकों का OTP दर्ज करें', mr: 'तुमच्या मोबाईलवर पाठवलेला 6 अंकी OTP टाका' },
  changeMobileNumber: { en: 'Change Mobile Number', hi: 'मोबाइल नंबर बदलें', mr: 'मोबाईल नंबर बदला' },
  enterRegisteredMobile: { en: 'Enter registered mobile number', hi: 'पंजीकृत मोबाइल नंबर दर्ज करें', mr: 'नोंदणीकृत मोबाईल नंबर टाका' },
  otpSentToMobile: { en: 'OTP has been sent to your mobile', hi: 'आपके मोबाइल पर OTP भेजा गया है', mr: 'तुमच्या मोबाईलवर OTP पाठवला आहे' },
  loginSuccess: { en: 'Login successful!', hi: 'लॉगिन सफल!', mr: 'लॉगिन यशस्वी!' },
  otpVerifyFailed: { en: 'OTP verification failed', hi: 'OTP सत्यापन विफल', mr: 'OTP सत्यापन अयशस्वी' },
  somethingWentWrong: { en: 'Something went wrong', hi: 'कुछ गलत हो गया', mr: 'काहीतरी चूक झाली' },
  enterMobile: { en: 'Please enter mobile number', hi: 'कृपया मोबाइल नंबर दर्ज करें', mr: 'कृपया मोबाईल नंबर टाका' },

  // Dashboard
  welcome: { en: 'Welcome', hi: 'स्वागत है', mr: 'स्वागत आहे' },
  todaysBills: { en: "Today's Bills", hi: 'आज के बिल', mr: 'आजचे बिल' },
  totalBills: { en: 'Total Bills', hi: 'Total Bills', mr: 'Total Bills' },
  createNewBill: { en: 'Create New Bill', hi: 'नया बिल बनाएं', mr: 'नवीन बिल तयार करा' },
  billHistory: { en: 'Bill History', hi: 'बिल इतिहास', mr: 'बिल इतिहास' },
  monthlyReports: { en: 'Monthly Reports', hi: 'मासिक रिपोर्ट', mr: 'मासिक अहवाल' },
  logout: { en: 'Logout', hi: 'लॉगआउट', mr: 'लॉगआउट' },

  // Bill Creation
  farmerDetails: { en: 'Farmer Details', hi: 'किसान विवरण', mr: 'शेतकरी तपशील' },
  farmerName: { en: 'Farmer Name', hi: 'किसान का नाम', mr: 'शेतकऱ्याचे नाव' },
  farmerMobile: { en: 'Farmer Mobile', hi: 'किसान मोबाइल', mr: 'शेतकरी मोबाईल' },
  address: { en: 'Address', hi: 'पता', mr: 'पत्ता' },
  vehicleNumber: { en: 'Vehicle Number', hi: 'वाहन नंबर', mr: 'वाहन क्रमांक' },

  weightCalculation: { en: 'Weight Calculation', hi: 'वजन गणना', mr: 'वजन गणना' },
  grossWeight: { en: 'Gross Weight (kg)', hi: 'कुल वजन (kg)', mr: 'एकूण वजन (kg)' },
  pattiWeight: { en: 'Patti Weight (kg)', hi: 'पत्ती वजन (kg)', mr: 'पत्ती वजन (kg)' },
  boxCount: { en: 'Box Count', hi: 'बॉक्स संख्या', mr: 'बॉक्स संख्या' },
  netWeight: { en: 'Net Weight (kg)', hi: 'शुद्ध वजन (kg)', mr: 'निव्वळ वजन (kg)' },
  dandaWeight: { en: 'Danda Weight (7%)', hi: 'दंडा वजन (7%)', mr: 'दंडा वजन (7%)' },
  tutWastage: { en: 'Tut/Wastage (kg)', hi: 'तूट/वाया (kg)', mr: 'तूट/वाया (kg)' },
  finalNetWeight: { en: 'Final Net Weight', hi: 'अंतिम शुद्ध वजन', mr: 'अंतिम निव्वळ वजन' },

  paymentCalculation: { en: 'Payment Calculation', hi: 'भुगतान गणना', mr: 'पेमेंट गणना' },
  ratePerKg: { en: 'Rate per Kg (₹)', hi: 'प्रति किलो दर (₹)', mr: 'प्रति किलो दर (₹)' },
  totalAmount: { en: 'Total Amount (₹)', hi: 'Total Amount (₹)', mr: 'Total Amount (₹)' },
  majuri: { en: 'Majuri (₹)', hi: 'मजूरी (₹)', mr: 'मजुरी (₹)' },
  netAmount: { en: 'Net Amount (₹)', hi: 'शुद्ध राशि (₹)', mr: 'निव्वळ रक्कम (₹)' },

  generateBill: { en: 'Generate Bill', hi: 'बिल बनाएं', mr: 'बिल तयार करा' },
  downloadPdf: { en: 'Download PDF', hi: 'PDF डाउनलोड', mr: 'PDF डाउनलोड' },
  shareWhatsapp: { en: 'Share on WhatsApp', hi: 'WhatsApp पर शेयर करें', mr: 'WhatsApp वर शेअर करा' },

  // Bill Preview
  billNumber: { en: 'Bill Number', hi: 'बिल नंबर', mr: 'बिल क्रमांक' },
  date: { en: 'Date', hi: 'तारीख', mr: 'तारीख' },

  // History
  searchByMobile: { en: 'Search by Mobile Number', hi: 'मोबाइल नंबर से खोजें', mr: 'मोबाईल नंबरने शोधा' },
  viewBill: { en: 'View Bill', hi: 'बिल देखें', mr: 'बिल पहा' },
  newBillForFarmer: { en: 'New Bill for Farmer', hi: 'किसान के लिए नया बिल', mr: 'शेतकऱ्यासाठी नवीन बिल' },
  noResults: { en: 'No bills found', hi: 'कोई बिल नहीं मिला', mr: 'बिल सापडले नाहीत' },

  // General
  save: { en: 'Save', hi: 'सेव करें', mr: 'सेव्ह करा' },
  cancel: { en: 'Cancel', hi: 'रद्द करें', mr: 'रद्द करा' },
  back: { en: 'Back', hi: 'वापस', mr: 'मागे' },
  kg: { en: 'kg', hi: 'kg', mr: 'kg' },
  rupees: { en: '₹', hi: '₹', mr: '₹' },

  // Reports Page
  month: { en: 'Month', hi: 'महीना', mr: 'महिना' },
  year: { en: 'Year', hi: 'वर्ष', mr: 'वर्ष' },
  generateReport: { en: 'Generate Report', hi: 'Generate Report', mr: 'Generate Report' },
  exportPdf: { en: 'Export PDF', hi: 'Export PDF', mr: 'Export PDF' },
  filterByDate: { en: 'Filter by Date', hi: 'Filter by Date', mr: 'Filter by Date' },
  hideDateFilter: { en: 'Hide Date Filter', hi: 'Hide Date Filter', mr: 'Hide Date Filter' },
  startDate: { en: 'Start Date', hi: 'Start Date', mr: 'Start Date' },
  endDate: { en: 'End Date', hi: 'End Date', mr: 'End Date' },
  apply: { en: 'Apply', hi: 'Apply', mr: 'Apply' },
  clearAll: { en: 'Clear All', hi: 'Clear All', mr: 'Clear All' },
  totalWeight: { en: 'Total Weight', hi: 'Total Weight', mr: 'Total Weight' },
  farmers: { en: 'Farmers', hi: 'Farmers', mr: 'Farmers' },
  topFarmers: { en: 'Top Farmers', hi: 'Top Farmers', mr: 'Top Farmers' },
  farmerWiseReport: { en: 'Farmer-wise Report (Search by Mobile)', hi: 'Farmer-wise Report (Search by Mobile)', mr: 'Farmer-wise Report (Search by Mobile)' },
  farmerReport: { en: 'Farmer Report', hi: 'किसान रिपोर्ट', mr: 'शेतकरी अहवाल' },
  searchFarmerByMobile: { en: 'Search Farmer by Mobile', hi: 'मोबाइल से किसान खोजें', mr: 'मोबाईल द्वारे शेतकरी शोधा' },
  allBills: { en: 'All Bills', hi: 'All Bills', mr: 'All Bills' },
  filteredBills: { en: 'Filtered Bills', hi: 'Filtered Bills', mr: 'Filtered Bills' },
  noBillsFound: { en: 'No bills found for', hi: 'No bills found for', mr: 'No bills found for' },
};

interface LanguageContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: string) => string;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const LanguageProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [language, setLanguage] = useState<Language>(() => {
    const saved = localStorage.getItem('banana-bill-language');
    return (saved as Language) || 'en';
  });

  const handleSetLanguage = (lang: Language) => {
    setLanguage(lang);
    localStorage.setItem('banana-bill-language', lang);
  };

  const t = (key: string): string => {
    const translation = translations[key];
    if (!translation) return key;
    return translation[language] || translation.en || key;
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage: handleSetLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
};

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (context === undefined) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
};

import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import { cloudinaryService } from './cloudinaryService';

/**
 * Export Service Abstraction Layer
 * Decouples third-party libraries from business logic
 * Easy to swap implementations without changing components
 */

export interface ExportOptions {
  filename?: string;
  quality?: number;
  scale?: number;
}

/**
 * Image Export Service
 */
class ImageExportService {
  /**
   * Convert HTML element to Blob
   */
  async toBlob(element: HTMLElement, options: ExportOptions = {}): Promise<Blob> {
    const { scale = 2, quality = 0.95 } = options;

    const canvas = await html2canvas(element, {
      scale,
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false,
    });

    return new Promise((resolve, reject) => {
      canvas.toBlob(
        (blob) => {
          if (blob) {
            resolve(blob);
          } else {
            reject(new Error('Failed to generate image'));
          }
        },
        'image/png',
        quality
      );
    });
  }

  /**
   * Convert HTML element to base64 data URL
   */
  async toBase64(element: HTMLElement, options: ExportOptions = {}): Promise<string> {
    const blob = await this.toBlob(element, options);
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => resolve(reader.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  }

  /**
   * Upload to cloud storage and return public URL
   */
  async toCloudUrl(element: HTMLElement, filename: string, options: ExportOptions = {}): Promise<string> {
    const blob = await this.toBlob(element, options);
    return cloudinaryService.uploadImage(blob, filename);
  }

  /**
   * Download image to user's device
   */
  async download(element: HTMLElement, filename: string, options: ExportOptions = {}): Promise<void> {
    const blob = await this.toBlob(element, options);
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = `${filename}.png`;
    a.click();

    URL.revokeObjectURL(url);
  }
}

/**
 * PDF Export Service
 */
class PDFExportService {
  /**
   * Convert HTML element to PDF and download
   */
  async download(element: HTMLElement, filename: string, options: ExportOptions = {}): Promise<void> {
    const { scale = 2 } = options;

    const canvas = await html2canvas(element, {
      scale,
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false,
    });

    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4',
    });

    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = pdf.internal.pageSize.getHeight();
    const imgWidth = canvas.width;
    const imgHeight = canvas.height;
    const ratio = Math.min(pdfWidth / imgWidth, pdfHeight / imgHeight);
    const imgX = (pdfWidth - imgWidth * ratio) / 2;
    const imgY = 10;

    pdf.addImage(imgData, 'PNG', imgX, imgY, imgWidth * ratio, imgHeight * ratio);
    pdf.save(`${filename}.pdf`);
  }

  /**
   * Convert HTML element to PDF Blob
   */
  async toBlob(element: HTMLElement, options: ExportOptions = {}): Promise<Blob> {
    const { scale = 2 } = options;

    const canvas = await html2canvas(element, {
      scale,
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false,
    });

    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4',
    });

    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = pdf.internal.pageSize.getHeight();
    const imgWidth = canvas.width;
    const imgHeight = canvas.height;
    const ratio = Math.min(pdfWidth / imgWidth, pdfHeight / imgHeight);
    const imgX = (pdfWidth - imgWidth * ratio) / 2;
    const imgY = 10;

    pdf.addImage(imgData, 'PNG', imgX, imgY, imgWidth * ratio, imgHeight * ratio);

    return pdf.output('blob');
  }
}

// Export singleton instances
export const imageExport = new ImageExportService();
export const pdfExport = new PDFExportService();

// ============================================================
// BACKWARD COMPATIBILITY
// Maintains old API for existing components
// ============================================================

/**
 * @deprecated Use imageExport.toBlob() instead
 */
export const generateBillImage = async (element: HTMLElement): Promise<Blob> => {
  return imageExport.toBlob(element);
};

/**
 * @deprecated Use pdfExport.download() instead
 */
export const generateBillPDF = async (element: HTMLElement, billNumber: string): Promise<void> => {
  return pdfExport.download(element, billNumber);
};

/**
 * @deprecated Use imageExport methods + cloudinaryService instead
 */
export const shareOnWhatsApp = async (element: HTMLElement, bill: any): Promise<void> => {
  try {
    const blob = await imageExport.toBlob(element);
    const file = new File([blob], `${bill.billNumber}.png`, { type: 'image/png' });

    // Check if Web Share API is available and can share files
    if (navigator.share && navigator.canShare && navigator.canShare({ files: [file] })) {
      await navigator.share({
        files: [file],
        title: `Bill ${bill.billNumber}`,
        text: `Bill from Banana Bill - Amount: ₹${(bill.netAmount || 0).toFixed(2)}`,
      });
    } else {
      // Fallback: Download the file
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${bill.billNumber}.png`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);

      // Open WhatsApp with pre-filled message
      const farmerMobile = bill.farmer?.mobileNumber || '';
      const message = encodeURIComponent(
        `*Banana Bill*\n` +
        `Bill No: ${bill.billNumber}\n` +
        `Amount: ₹${(bill.netAmount || 0).toFixed(2)}\n\n` +
        `Please check the attached bill image.`
      );

      const phoneNumber = farmerMobile.startsWith('91') ? farmerMobile : `91${farmerMobile}`;
      const whatsappUrl = `https://wa.me/${phoneNumber}?text=${message}`;

      window.open(whatsappUrl, '_blank');
    }
  } catch (error) {
    console.error('Error sharing on WhatsApp:', error);
    throw error;
  }
};

// Default export for backward compatibility
export default {
  image: imageExport,
  pdf: pdfExport,
  // Old exports
  generateBillImage,
  generateBillPDF,
  shareOnWhatsApp,
};

/**
 * Cloudinary Image Upload Service
 * Uploads bill images to Cloudinary and returns public URL
 */

export interface CloudinaryUploadResponse {
    secure_url: string;
    public_id: string;
    width: number;
    height: number;
}

class CloudinaryService {
    private cloudName: string = '';
    private uploadPreset: string = '';

    /**
     * Initialize Cloudinary configuration
     * Call this once with your Cloudinary credentials
     */
    configure(cloudName: string, uploadPreset: string) {
        this.cloudName = cloudName;
        this.uploadPreset = uploadPreset;
    }

    /**
     * Upload image blob to Cloudinary
     * Returns secure HTTPS URL for the uploaded image
     */
    async uploadImage(blob: Blob, filename: string = 'bill'): Promise<string> {
        if (!this.cloudName || !this.uploadPreset) {
            throw new Error('Cloudinary not configured. Call configure() first.');
        }

        try {
            const formData = new FormData();
            formData.append('file', blob, `${filename}.png`);
            formData.append('upload_preset', this.uploadPreset);
            formData.append('folder', 'banana-bills'); // Organize in folder

            const response = await fetch(
                `https://api.cloudinary.com/v1_1/${this.cloudName}/image/upload`,
                {
                    method: 'POST',
                    body: formData,
                }
            );

            if (!response.ok) {
                throw new Error(`Upload failed: ${response.statusText}`);
            }

            const data: CloudinaryUploadResponse = await response.json();
            return data.secure_url;
        } catch (error) {
            console.error('Cloudinary upload error:', error);
            throw new Error('Failed to upload image to Cloudinary');
        }
    }

    /**
     * Upload bill image with automatic naming
     * Uses bill number for filename
     */
    async uploadBillImage(blob: Blob, billNumber: string): Promise<string> {
        const filename = `bill_${billNumber}_${Date.now()}`;
        return this.uploadImage(blob, filename);
    }
}

// Export singleton instance
export const cloudinaryService = new CloudinaryService();

// ✅ Initialize with your Cloudinary credentials
cloudinaryService.configure('dpovgzwtl', 'banana-bills');

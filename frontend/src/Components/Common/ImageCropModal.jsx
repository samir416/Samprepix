import { useState } from "react";
import getCroppedImg from "../../utils/cropImage";
import Cropper from "react-easy-crop";


export default function ImageCropModal({
    isOpen,
    image,
    onClose,
    onSave
}) {

    const [crop, setCrop] = useState({ x: 0, y: 0 });

    const [zoom, setZoom] = useState(1);

    const [croppedAreaPixels, setCroppedAreaPixels] = useState(null);

    const [isSaving, setIsSaving] = useState(false);

    const handleSave = async () => {

        if (!croppedAreaPixels) return;

        try {

            setIsSaving(true);

            const croppedFile = await getCroppedImg(
                image,
                croppedAreaPixels
            );

            onSave(croppedFile);

        } finally {

            setIsSaving(false);

        }

    };


    if (!isOpen || !image) return null;

    return (
        <div className="image-crop-modal-backdrop">

            <div className="image-crop-modal">

                <div className="image-crop-header">

                    <h2>Crop Profile Picture</h2>

                </div>

                <div className="image-crop-body">

                    <Cropper
                        image={image}
                        crop={crop}
                        zoom={zoom}
                        aspect={1}
                        cropShape="round"
                        showGrid
                        onCropChange={setCrop}
                        onZoomChange={setZoom}
                        onCropComplete={(_, croppedPixels) =>
                            setCroppedAreaPixels(croppedPixels)
                        }
                    />

                </div>

                <div className="image-crop-footer">

                    <input
                        type="range"
                        min={1}
                        max={3}
                        step={0.1}
                        value={zoom}
                        onChange={(e) =>
                            setZoom(Number(e.target.value))
                        }
                    />

                    <button
                        type="button"
                        onClick={onClose}
                    >
                        Cancel
                    </button>

                    <button
                        type="button"
                        onClick={handleSave}
                        disabled={isSaving}
                    >
                        {isSaving ? "Processing..." : "Save"}
                    </button>

                </div>

            </div>

        </div>
    );
}

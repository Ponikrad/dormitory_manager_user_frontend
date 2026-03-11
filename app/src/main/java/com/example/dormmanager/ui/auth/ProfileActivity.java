package com.example.dormmanager.ui.auth;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.dormmanager.R;
import com.example.dormmanager.data.local.TokenManager;
import com.example.dormmanager.data.model.ResidentCard;
import com.example.dormmanager.data.model.User;
import com.example.dormmanager.data.repository.AuthRepository;
import com.example.dormmanager.data.repository.ProfileRepository;
import com.example.dormmanager.data.repository.ResidentCardRepository;
import com.example.dormmanager.utils.ImageValidationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private MaterialToolbar toolbar;
    private ImageView ivProfileImage, ivQrCode;
    private ProgressBar imageLoadingProgress, qrLoadingProgress;
    private MaterialButton btnChangePhoto, btnDeletePhoto, btnEditProfile, btnChangePassword;
    private TextView tvUsername, tvEmail, tvFullName, tvPhoneNumber, tvRoomNumber, tvCreatedAt;
    private TextView tvCardNumber;
    private Chip chipCardStatus;
    private TextView tvCardExpiry;
    private MaterialButton btnGenerateCard;
    private Button btnLogout;
    private View loadingOverlay;


    private AuthRepository authRepository;
    private ResidentCardRepository cardRepository;
    private ProfileRepository profileRepository;
    private TokenManager tokenManager;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private File tempImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupToolbar();
        initRepositories();
        setupImageLaunchers();
        setupListeners();
        loadProfile();
        loadProfileImage();
        loadResidentCard();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        imageLoadingProgress = findViewById(R.id.imageLoadingProgress);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnDeletePhoto = findViewById(R.id.btnDeletePhoto);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvFullName = findViewById(R.id.tvFullName);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);

        tvCardNumber = findViewById(R.id.tvCardNumber);
        tvCardExpiry = findViewById(R.id.tvCardExpiry);
        chipCardStatus = findViewById(R.id.chipCardStatus);
        ivQrCode = findViewById(R.id.ivQrCode);
        qrLoadingProgress = findViewById(R.id.qrLoadingProgress);
        btnGenerateCard = findViewById(R.id.btnGenerateCard);

        btnLogout = findViewById(R.id.btnLogout);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initRepositories() {
        authRepository = new AuthRepository(this);
        cardRepository = new ResidentCardRepository(this);
        profileRepository = new ProfileRepository(this);
        tokenManager = new TokenManager(this);
    }

    private void setupImageLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageFromUri(imageUri);
                        }
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (tempImageFile != null && tempImageFile.exists()) {
                            uploadImage(tempImageFile);
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        btnChangePhoto.setOnClickListener(v -> showImageSourceDialog());
        btnDeletePhoto.setOnClickListener(v -> deleteProfileImage());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> logout());
        btnGenerateCard.setOnClickListener(v -> generateNewCard());
    }

    private void showImageSourceDialog() {
        View dialogView = LayoutInflater.from(this).inflate(android.R.layout.select_dialog_item, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source");

        TextView infoText = new TextView(this);
        infoText.setText("📌 Image requirements:\n• Format: JPEG or PNG\n• Max size: 5MB\n• Recommended: Square, 500x500px+");
        infoText.setPadding(60, 30, 60, 30);
        infoText.setTextSize(13);
        infoText.setTextColor(getResources().getColor(R.color.md_theme_dark_onSurfaceVariant));
        builder.setCustomTitle(infoText);

        builder.setItems(new String[]{"🖼️ Gallery"}, (dialog, which) -> {
            openImagePicker();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Camera Not Available")
                    .setMessage("No camera app found on your device.\n\nPlease use the gallery option instead.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        try {
            tempImageFile = File.createTempFile("profile_", ".jpg", getCacheDir());
            Uri photoUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    tempImageFile
            );
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraLauncher.launch(cameraIntent);
        } catch (IOException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Failed to prepare camera.\n\nPlease try again or use the gallery option.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageFromUri(Uri imageUri) {
        try {
            ImageValidationHelper.ValidationResult validation =
                    ImageValidationHelper.validateImage(this, imageUri);

            if (!validation.isValid) {
                showValidationErrorDialog(validation);
                return;
            }

            String fileName = ImageValidationHelper.getFileName(this, imageUri);
            long fileSize = ImageValidationHelper.getFileSize(this, imageUri);
            String sizeStr = ImageValidationHelper.formatFileSize(fileSize);

            Toast.makeText(this, "Processing: " + fileName + " (" + sizeStr + ")",
                    Toast.LENGTH_SHORT).show();

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                showErrorDialog("Failed to decode image",
                        "The selected file may be corrupted.\n\nPlease try another image.");
                return;
            }

            if (bitmap.getWidth() > ImageValidationHelper.MAX_DIMENSION ||
                    bitmap.getHeight() > ImageValidationHelper.MAX_DIMENSION) {
                showErrorDialog("Image Too Large",
                        ImageValidationHelper.getValidationErrorMessage(
                                ImageValidationHelper.ValidationError.DIMENSIONS_TOO_LARGE));
                return;
            }

            File imageFile = new File(getCacheDir(), "profile_temp.jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);

            int quality = fileSize > 2 * 1024 * 1024 ? 70 : 85; // More compression for larger files
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();

            uploadImage(imageFile);

        } catch (IOException e) {
            showErrorDialog("Processing Error",
                    "Failed to process the selected image.\n\nPlease try again.");
        }
    }

    private void showValidationErrorDialog(ImageValidationHelper.ValidationResult validation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String title = "Invalid Image";
        switch (validation.error) {
            case INVALID_TYPE:
                title = "Invalid File Type";
                break;
            case FILE_TOO_LARGE:
                title = "File Too Large";
                break;
            case FILE_EMPTY:
                title = "Empty File";
                break;
            case DIMENSIONS_TOO_LARGE:
                title = "Image Too Large";
                break;
        }

        builder.setTitle(title)
                .setMessage(validation.message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", null);

        if (validation.error == ImageValidationHelper.ValidationError.INVALID_TYPE ||
                validation.error == ImageValidationHelper.ValidationError.FILE_TOO_LARGE) {
            builder.setNeutralButton("Requirements", (dialog, which) -> showImageRequirementsDialog());
        }

        builder.show();
    }

    private void showImageRequirementsDialog() {
        String requirements =
                "📋 Image Requirements:\n\n" +
                        "✓ Format: JPEG or PNG\n" +
                        "✓ Maximum size: 5MB\n" +
                        "✓ Maximum dimensions: 4096x4096\n" +
                        "✓ Recommended: Square image\n" +
                        "✓ Recommended: At least 500x500px\n\n" +
                        "💡 Tips:\n" +
                        "• Use image compression apps if your file is too large\n" +
                        "• Crop to square for best results\n" +
                        "• Good lighting makes better profile photos!";

        new AlertDialog.Builder(this)
                .setTitle("Image Requirements")
                .setMessage(requirements)
                .setPositiveButton("Got it", null)
                .show();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean isValidImageType(String mimeType) {
        return ImageValidationHelper.isValidMimeType(mimeType);
    }

    private long getFileSizeFromUri(Uri uri) {
        return ImageValidationHelper.getFileSize(this, uri);
    }

    private void showInvalidFileTypeDialog() {
        showValidationErrorDialog(ImageValidationHelper.ValidationResult.failure(
                ImageValidationHelper.ValidationError.INVALID_TYPE));
    }

    private void showFileTooLargeDialog() {
        showValidationErrorDialog(ImageValidationHelper.ValidationResult.failure(
                ImageValidationHelper.ValidationError.FILE_TOO_LARGE));
    }

    private void uploadImage(File imageFile) {
        if (!imageFile.exists()) {
            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageFile.length() == 0) {
            Toast.makeText(this, "Image file is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        showImageLoading(true);
        profileRepository.uploadProfileImage(imageFile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showImageLoading(false);
                    Toast.makeText(ProfileActivity.this, "✓ " + message, Toast.LENGTH_SHORT).show();
                    loadProfileImage();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showImageLoading(false);

                    String userMessage = parseErrorMessage(error);

                    new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle("Upload Failed")
                            .setMessage(userMessage)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }

    private String parseErrorMessage(String error) {
        if (error == null) return "Unknown error occurred";

        if (error.contains("Invalid file type") || error.contains("file type")) {
            return "Invalid file type.\n\nOnly JPEG and PNG images are allowed.";
        }

        if (error.contains("exceeds maximum") || error.contains("too large") || error.contains("5MB")) {
            return "File is too large.\n\nMaximum size: 5MB\n\nPlease compress the image or choose a smaller one.";
        }

        if (error.contains("Network") || error.contains("network")) {
            return "Network error.\n\nPlease check your internet connection and try again.";
        }

        if (error.contains("404")) {
            return "Server endpoint not found.\n\nPlease contact support.";
        }

        if (error.contains("401") || error.contains("Unauthorized")) {
            return "Session expired.\n\nPlease log in again.";
        }

        return "Upload failed: " + error;
    }

    private void loadProfileImage() {
        showImageLoading(true);
        profileRepository.getProfileImage(new ProfileRepository.ImageCallback() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                runOnUiThread(() -> {
                    showImageLoading(false);
                    ivProfileImage.setImageBitmap(bitmap);
                    btnDeletePhoto.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onImageNotFound() {
                runOnUiThread(() -> {
                    showImageLoading(false);
                    ivProfileImage.setImageResource(R.drawable.ic_profile);
                    btnDeletePhoto.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showImageLoading(false);
                    ivProfileImage.setImageResource(R.drawable.ic_profile);
                    btnDeletePhoto.setVisibility(View.GONE);
                });
            }
        });
    }

    private void deleteProfileImage() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile Photo")
                .setMessage("Are you sure you want to delete your profile photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    showImageLoading(true);
                    profileRepository.deleteProfileImage(new ProfileRepository.ProfileCallback() {
                        @Override
                        public void onSuccess(String message) {
                            runOnUiThread(() -> {
                                showImageLoading(false);
                                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                                ivProfileImage.setImageResource(R.drawable.ic_profile);
                                btnDeletePhoto.setVisibility(View.GONE);
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                showImageLoading(false);
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);

        TextInputEditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etLastName);
        TextInputEditText etPhoneNumber = dialogView.findViewById(R.id.etPhoneNumber);

        etFirstName.setText(tokenManager.getFirstName());
        etLastName.setText(tokenManager.getLastName());
        etPhoneNumber.setText(tokenManager.getPhoneNumber());
        
        // Hide room number field - only admin can change it
        View tilRoomNumber = dialogView.findViewById(R.id.tilRoomNumber);
        if (tilRoomNumber != null) {
            tilRoomNumber.setVisibility(View.GONE);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            // Room number is not editable by user - only admin can change it

            showLoading(true);
            profileRepository.updateProfile(firstName, lastName, phoneNumber, null,
                    new ProfileRepository.ProfileCallback() {
                        @Override
                        public void onSuccess(String message) {
                            runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadProfile();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });

        dialog.show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);

        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoading(true);
            profileRepository.changePassword(currentPassword, newPassword, confirmPassword,
                    new ProfileRepository.ProfileCallback() {
                        @Override
                        public void onSuccess(String message) {
                            runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });

        dialog.show();
    }

    private void loadProfile() {
        showLoading(true);

        authRepository.getProfile(new AuthRepository.AuthCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    showLoading(false);
                    displayUserInfo(user);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    displayCachedUserInfo();
                });
            }
        });
    }

    private void displayUserInfo(User user) {
        tvUsername.setText(user.getUsername() != null ? user.getUsername() : "N/A");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");

        String fullName = user.getFirstName() + " " + user.getLastName();
        tvFullName.setText(fullName);

        String phone = user.getPhoneNumber();
        tvPhoneNumber.setText(phone != null && !phone.isEmpty() ? phone : "Not set");

        String room = user.getRoomNumber();
        tvRoomNumber.setText(room != null && !room.isEmpty() ? room : "Not assigned");

        String createdAt = user.getCreatedAt();
        if (createdAt != null && createdAt.length() >= 10) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(createdAt);
                tvCreatedAt.setText(outputFormat.format(date));
            } catch (ParseException e) {
                tvCreatedAt.setText(createdAt.substring(0, 10));
            }
        } else {
            tvCreatedAt.setText("Not available");
        }
    }

    private void displayCachedUserInfo() {
        tvUsername.setText(tokenManager.getUsername() != null ? tokenManager.getUsername() : "N/A");
        tvEmail.setText(tokenManager.getEmail() != null ? tokenManager.getEmail() : "N/A");
        tvFullName.setText(tokenManager.getFullName() != null ? tokenManager.getFullName() : "N/A");

        String cachedPhone = tokenManager.getPhoneNumber();
        tvPhoneNumber.setText(cachedPhone != null && !cachedPhone.isEmpty() ? cachedPhone : "Not set");

        String cachedRoom = tokenManager.getRoomNumber();
        tvRoomNumber.setText(cachedRoom != null && !cachedRoom.isEmpty() ? cachedRoom : "Not assigned");

        String cachedCreatedAt = tokenManager.getCreatedAt();
        if (cachedCreatedAt != null && cachedCreatedAt.length() >= 10) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(cachedCreatedAt);
                tvCreatedAt.setText(outputFormat.format(date));
            } catch (ParseException e) {
                tvCreatedAt.setText(cachedCreatedAt.substring(0, 10));
            }
        } else {
            tvCreatedAt.setText("Not available");
        }
    }

    private void loadResidentCard() {
        qrLoadingProgress.setVisibility(View.VISIBLE);
        ivQrCode.setVisibility(View.GONE);
        btnGenerateCard.setVisibility(View.GONE);

        cardRepository.getMyCard(new ResidentCardRepository.CardCallback() {
            @Override
            public void onSuccess(ResidentCard card) {
                runOnUiThread(() -> {
                    qrLoadingProgress.setVisibility(View.GONE);
                    displayResidentCard(card);
                });
            }

            @Override
            public void onCardNotFound() {
                runOnUiThread(() -> {
                    qrLoadingProgress.setVisibility(View.GONE);
                    showGenerateCardOption();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    qrLoadingProgress.setVisibility(View.GONE);
                    showGenerateCardOption();
                });
            }
        });
    }

    private void displayResidentCard(ResidentCard card) {
        if (card.getQrCode() != null) {
            Bitmap qrBitmap = generateQRCode(card.getQrCode());
            if (qrBitmap != null) {
                ivQrCode.setImageBitmap(qrBitmap);
                ivQrCode.setVisibility(View.VISIBLE);
            }
        }

        tvCardNumber.setText("Card: " + (card.getQrCode() != null ? card.getQrCode() : "N/A"));
        String status = card.getDisplayStatus();
        chipCardStatus.setText(status);

        int backgroundColorRes;
        int textColor = Color.WHITE;

        if (status.equalsIgnoreCase("Active")) {
            backgroundColorRes = R.color.status_success;
        } else if (status.equalsIgnoreCase("Expired")) {
            backgroundColorRes = R.color.status_error;
        } else if (status.equalsIgnoreCase("Expiring Soon")) {
            backgroundColorRes = R.color.status_warning;
        } else {
            backgroundColorRes = R.color.md_theme_light_surfaceVariant;
            textColor = ContextCompat.getColor(this, R.color.text_primary);
        }

        int backgroundColor = ContextCompat.getColor(this, backgroundColorRes);
        chipCardStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(backgroundColor));
        chipCardStatus.setTextColor(textColor);
        chipCardStatus.setChipIconTint(android.content.res.ColorStateList.valueOf(textColor));

        String expiryDate = card.getExpirationDate();
        if (expiryDate != null && expiryDate.length() >= 10) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
                Date date = inputFormat.parse(expiryDate);
                if (tvCardExpiry != null) {
                    tvCardExpiry.setText("Valid until: " + outputFormat.format(date));
                }
            } catch (ParseException e) {
                if (tvCardExpiry != null) {
                    tvCardExpiry.setText("Valid until: " + expiryDate.substring(0, 10));
                }
            }
        } else {
            if (tvCardExpiry != null) {
                tvCardExpiry.setText("Valid until: N/A");
            }
        }

        btnGenerateCard.setVisibility(View.GONE);
    }
    private void showGenerateCardOption() {
        tvCardNumber.setText("No resident card");
        chipCardStatus.setText("Status: Not generated");
        btnGenerateCard.setVisibility(View.VISIBLE);
        ivQrCode.setVisibility(View.GONE);
    }

    private void generateNewCard() {
        showLoading(true);

        cardRepository.generateCard(new ResidentCardRepository.CardCallback() {
            @Override
            public void onSuccess(ResidentCard card) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Resident card generated!", Toast.LENGTH_SHORT).show();
                    displayResidentCard(card);
                });
            }

            @Override
            public void onCardNotFound() {
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Failed to generate card: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private Bitmap generateQRCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void logout() {
        authRepository.logout();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showImageLoading(boolean show) {
        imageLoadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadProfileImage();
        loadResidentCard();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
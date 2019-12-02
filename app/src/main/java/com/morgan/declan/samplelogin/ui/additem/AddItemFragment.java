package com.morgan.declan.samplelogin.ui.additem;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.morgan.declan.samplelogin.Post;
import com.morgan.declan.samplelogin.R;
import com.morgan.declan.samplelogin.Upload;
import com.morgan.declan.samplelogin.ui.home.HomeViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Adds a post instance to Firebase Database.
 * */
public class AddItemFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabase;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private String currentPhotoPath;

    private EditText et_title;
    private EditText et_desc;
    private String et_title_text;
    private String et_desc_text;

    private Button buttonChoose;
    private Button buttonUpload;
    private Button buttonTakePhoto;
    //uri to store file
    private Uri filePath;
    private View rootView;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private Spinner sizeSpinner, conditionSpinner;
    private String sizeSelected, conditionSelected;

    private AddItemViewModel addItemViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        addItemViewModel =
                ViewModelProviders.of(this).get(AddItemViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_additem, container, false);

        //Refereces each of the buttons in the fragment layout.
        buttonChoose = root.findViewById(R.id.choose_image);
        buttonTakePhoto = root.findViewById(R.id.take_photo);
        buttonUpload = root.findViewById(R.id.upload_post_btn);

        rootView = root;
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        sizeSpinner = root.findViewById(R.id.addItemSize);
        conditionSpinner = root.findViewById(R.id.add_item_condition);

        //Populates the size spinner with each size from the strings xml file.
        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sizeSelected = sizeSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        //Populates the condition spinner with each condition from the strings xml file.
        conditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                conditionSelected = conditionSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                conditionSelected = null;
            }

        });

        //Calls the show file chooser method to choose an image from the gallery.
        buttonChoose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        //calls the upload file method to upload the post to Firebase.
        buttonUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    uploadFile(root);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //Calls the take picture method to allow user to capture a picture from the camera.
        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        return root;
    }

    /**
     * Calls an intent to show a file chooser to allow user to choose an image from
     * the gallery to upload as part of a post to Firebase.
     * */
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 234);
    }

    /**
     * Calls an intent to open the camera app to allow the user to capture an image
     * of a product to be uploaded as part of a post to Firebase.
     * */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Creates a file from with the image captured from the camera intent and stores it on
     * the users device.
     *
     * @return File stored on the users device taken from the camera.
     * */
    private File createImageFile() throws IOException {
        // Create an image file name with a date stamp.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //Get the directory to store the image in.
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //Creating a temporary image file.
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        // Save a file in the directory on the users device.
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Gets intent data from the camera when a picture has been taken and displays the image
     * in the image view in the activity.
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Getting the data from the intent when a file is choosen with the file chooser.
        if (requestCode == 234 && data != null && data.getData() != null) {
            filePath = data.getData();
            Uri uri = data.getData();
            // Retrieve the image from the intent and populate the image view in the activity.
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                ImageView imageView = rootView.findViewById(R.id.image_preview);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Gets the data from the intent when a file is captured with the camera intent.
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK  ) {
            File imgFile = new  File(currentPhotoPath);
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView imageView = rootView.findViewById(R.id.image_preview);
            imageView.setImageBitmap(myBitmap);
            filePath = Uri.fromFile(imgFile);
            galleryAddPic();
        }
    }

    /**
     * Saves the file captured from the camera intent to the users phones gallery.
     * */
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        currentPhotoPath = f.getPath();
        mediaScanIntent.setData(contentUri);
        getContext().sendBroadcast(mediaScanIntent);
    }

    /**
     * Gets the file extension of a file.
     *
     * @param uri file path for the image.
     * @return file extension in the form of a string.
     * */
    public String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /**
     * Uploads a post to Firebase with all the neccessary information including the image
     * representing the post.
     *
     * @param root the root view of the activity.
     * */
    private void uploadFile(View root) throws IOException {
        //Initialise each field in the activity and get the text from each field to store in a post.
        EditText nameText = root.findViewById(R.id.addItemName);
        String name = nameText.getText().toString();
        EditText brandText = root.findViewById(R.id.addItemBrand);
        String brand = brandText.getText().toString();
        EditText colourText = root.findViewById(R.id.addItemColour);
        String colour = colourText.getText().toString();
        EditText descriptionText = root.findViewById(R.id.addItemDescription);
        String description = descriptionText.getText().toString();

        //Upload the post only if an image has been choosen.
        if ((filePath != null) && !(name.equals("")) && !(brand.equals("")) && !(colour.equals("")) && !(description.equals(""))) {

            //displaying progress dialog while image is uploading
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            //Create a new post instance with all the strings from each field.
            final Post myPost =  new Post(name,
                    sizeSelected,
                    colour,
                    brand, conditionSelected,
                    description,
                    currentUser.getUid());
            //getting the firebase storage reference.
            myPost.setPid();
            final StorageReference sRef = mStorageReference.child("uploads/" + myPost.getPid() + "." + getFileExtension(filePath));

            //Compress the size of the image to reduce upload time.
            Bitmap bmpImage = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmpImage.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] data = baos.toByteArray();


            //adding the file to Firebase
            sRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Get the download URL of the image.
                                    final Uri downloadUri = uri;
                                    //dismissing the progress dialog
                                    progressDialog.dismiss();

                                    //displaying a success toast
                                    Toast.makeText(getContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                                    //creating the upload object to store uploaded image details
                                    Upload upload = new Upload("example_testIMG", downloadUri.toString(), myPost.getPid());
                                    //upload the post to Firebase.
                                    myPost.postToDatabase(myPost);


                                    //Uploading the image to Firebase Storage.
                                    mDatabase.child("picture_uploads").child(currentUser.getUid()).child(myPost.getPid()).setValue(upload);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //Display the exception if upload fails.
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress dialog to the user.
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        } else {
            //Inform the user to add an image before uploading a post.
            Toast.makeText(getActivity(), "Complete all fields before posting", Toast.LENGTH_SHORT).show();
        }
    }
}
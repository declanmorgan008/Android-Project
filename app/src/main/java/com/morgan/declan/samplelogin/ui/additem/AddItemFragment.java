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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.morgan.declan.samplelogin.ItemArrayAdapter;
import com.morgan.declan.samplelogin.Post;
import com.morgan.declan.samplelogin.R;
import com.morgan.declan.samplelogin.Upload;
import com.morgan.declan.samplelogin.ui.home.HomeViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
    private FloatingActionButton buttonUpload;
    private ImageButton buttonTakePhoto;
    //uri to store file
    private Uri filePath;
    private View rootView;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private Spinner sizeSpinner;
    private String sizeSelected;

    private AddItemViewModel addItemViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        addItemViewModel =
                ViewModelProviders.of(this).get(AddItemViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_additem, container, false);

        buttonChoose = root.findViewById(R.id.choose_image);
        buttonTakePhoto = root.findViewById(R.id.take_photo);
        buttonUpload = root.findViewById(R.id.floatingAddPostButton);

        rootView = root;
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        sizeSpinner = root.findViewById(R.id.addItemSize);

        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sizeSelected = sizeSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        buttonChoose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

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
        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        return root;
    }


    private void postItem(View root){
        EditText nameText = root.findViewById(R.id.addItemName);
        String name = nameText.getText().toString();
//        sizeSpinner = root.findViewById(R.id.addItemSize);
//        String size = sizeText.getText().toString();
        EditText brandText = root.findViewById(R.id.addItemBrand);
        String brand = brandText.getText().toString();
        EditText colourText = root.findViewById(R.id.addItemColour);
        String colour = colourText.getText().toString();
        EditText conditionText = root.findViewById(R.id.addItemCondition);
        String condition = conditionText.getText().toString();
        EditText descriptionText = root.findViewById(R.id.addItemDescription);
        String description = descriptionText.getText().toString();
        Post post = new Post(name,sizeSelected,colour,brand,condition,description);
        post.postToDatabase(post);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 234);
    }
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


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Toast.makeText(getContext(), currentPhotoPath, Toast.LENGTH_LONG).show();
        return image;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 234 && data != null && data.getData() != null) {
            filePath = data.getData();
            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                ImageView imageView = rootView.findViewById(R.id.image_preview);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK  ) {

//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            File imgFile = new  File(currentPhotoPath);
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView imageView = rootView.findViewById(R.id.image_preview);
            imageView.setImageBitmap(myBitmap);
       //     File file = new File(Environment.getExternalStorageDirectory().getPath(), currentPhotoPath);
            filePath = Uri.fromFile(imgFile);
            galleryAddPic();
        }
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        currentPhotoPath = f.getPath();
        mediaScanIntent.setData(contentUri);
        getContext().sendBroadcast(mediaScanIntent);
    }


    public String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(View root) throws IOException {

        EditText nameText = root.findViewById(R.id.addItemName);
        String name = nameText.getText().toString();
        EditText brandText = root.findViewById(R.id.addItemBrand);
        String brand = brandText.getText().toString();
        EditText colourText = root.findViewById(R.id.addItemColour);
        String colour = colourText.getText().toString();
        EditText conditionText = root.findViewById(R.id.addItemCondition);
        String condition = conditionText.getText().toString();
        EditText descriptionText = root.findViewById(R.id.addItemDescription);
        String description = descriptionText.getText().toString();


        //checking if file is available
        if (filePath != null) {
            //displaying progress dialog while image is uploading
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            final Post myPost =  new Post(name,
                    sizeSelected,
                    colour,
                    brand, condition,
                    description,
                    currentUser.getUid());
            //getting the storage reference
            myPost.setPid();
            Log.e("test",filePath.toString());
            final StorageReference sRef = mStorageReference.child("uploads/" + myPost.getPid() + "." + getFileExtension(filePath));

            Bitmap bmpImage = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmpImage.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] data = baos.toByteArray();

            //adding the file to reference
            sRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final Uri downloadUri = uri;
                                    //dismissing the progress dialog
                                    progressDialog.dismiss();

                                    //displaying success toast
                                    Toast.makeText(getContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                                    //creating the upload object to store uploaded image details
                                    Upload upload = new Upload("example_testIMG", downloadUri.toString(), myPost.getPid());
                                    //myPost.setPicture(downloadUri.toString());
                                    myPost.postToDatabase(myPost);


                                    //adding an upload to firebase database
                                    //String uploadId = mDatabase.push().getKey();
                                    mDatabase.child("picture_uploads").child(currentUser.getUid()).child(myPost.getPid()).setValue(upload);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "Select a picture and try again.", Toast.LENGTH_SHORT).show();
        }
    }



}
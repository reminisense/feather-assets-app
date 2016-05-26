package com.reminisense.fa.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.reminisense.fa.BuildConfig;
import com.reminisense.fa.R;
import com.reminisense.fa.managers.CacheManager;
import com.reminisense.fa.models.VerifyRequest;
import com.reminisense.fa.models.VerifyResult;
import com.reminisense.fa.utils.FeatherAssetsWebService;
import com.reminisense.fa.utils.RestClient;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 5/24/2016.
 */
public class ScanActivity extends AppCompatActivity {

    /*
    Where the Verification Data Displays
     */
    @Bind(R.id.btnQr)
    AppCompatButton btnQr;
    @Bind(R.id.btnBarcode)
    AppCompatButton btnBarcode;
    @Bind(R.id.btnRfid)
    AppCompatButton btnRfid;
    @Bind(R.id.image)
    ImageView image;
    @Bind(R.id.assetNameData)
    TextView assetNameData;
    @Bind(R.id.ownerNameData)
    TextView ownerNameData;
    @Bind(R.id.descriptionData)
    TextView descriptionData;
    @Bind(R.id.takeOutAvailData)
    TextView takeOutAvailData;
    @Bind(R.id.takeOutNoteData)
    TextView takeOutNoteData;

    private static final int SCAN_RFID = 1;
    private static final int SCAN_BARCODE = 2;
    private static final int SCAN_QR = 3;

    private FeatherAssetsWebService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        ButterKnife.bind(this);

        //initialize api service
        apiService = new RestClient().getApiService();

        btnQr.setOnClickListener(new QrListener());
        btnBarcode.setOnClickListener(new BarcodeListener());
        btnRfid.setOnClickListener(new RfidListener());
    }

    // FIXME transfer these listeners to their own classes because there are code duplications
    private class QrListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".activities.BarcodeScannerActivity");
            startActivityForResult(intent, SCAN_QR);
        }
    }

    private class BarcodeListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".activities.BarcodeScannerActivity");
            startActivityForResult(intent, SCAN_BARCODE);
        }
    }

    private class RfidListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".activities.NfcScannerActivity");
            startActivityForResult(intent, SCAN_RFID);
        }
    }


    private Bitmap getBitmap(String strPath) {
        File mediaFile = new File(strPath);
        Uri captureImageUri = Uri.fromFile(mediaFile);

        image.setVisibility(View.VISIBLE);

        // bimatp factory
        BitmapFactory.Options options = new BitmapFactory.Options();

        // downsizing image as it throws OutOfMemory Exception for larger
        // images
        options.inSampleSize = 8;

        final Bitmap bitmap = BitmapFactory.decodeFile(captureImageUri.getPath(),
                options);
        return bitmap;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            VerifyRequest request = new VerifyRequest();
            int companyId = CacheManager.retrieveCompanyId(ScanActivity.this);
            // FIXME: we are assuming that there is a company with id = 1
            request.setCompanyId(companyId == 0 ? 1 : companyId);
            request.setTag(data.getDataString());
            if (requestCode == SCAN_RFID) {
                request.setTagType(1);
            } else if (requestCode == SCAN_BARCODE) {
                request.setTagType(2);
            } else if (requestCode == SCAN_QR) {
                request.setTagType(3);
            }

            Call<VerifyResult> call = apiService.verify(request, CacheManager.retrieveAuthToken(ScanActivity.this));
            call.enqueue(new Callback<VerifyResult>() {
                @Override
                public void onResponse(Call<VerifyResult> call, Response<VerifyResult> response) {
                    if (response.code() == 200) {

                        VerifyResult verifyResult = response.body();
                        Log.d(RegisterActivity.class.toString(), verifyResult.toString());

                        if ("OK".equals(verifyResult.getResult())) {
                        /*
                        get data from database and display
                         */
                            image.setImageBitmap(getBitmap(verifyResult.getImageUrls()));
                            assetNameData.setText(verifyResult.getName());
                            ownerNameData.setText(verifyResult.getDescription());
                            descriptionData.setText(verifyResult.getDescription());
                            // TODO takeOutAllowed
                            takeOutNoteData.setText(verifyResult.getTakeOutInfo());
                        } else {
                            Toast.makeText(ScanActivity.this, "Asset not found! Please register.", Toast.LENGTH_LONG).show();
                        }
                    } else if (response.code() == 401) {
                        Toast.makeText(ScanActivity.this, "Authentication error.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ScanActivity.this, "Error: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<VerifyResult> call, Throwable t) {
                    Toast.makeText(ScanActivity.this, "Error connecting to server, please try again", Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                }

            });
        }
    }
}
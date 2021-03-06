package com.example.testepicpic.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.testepicpic.R;
import com.example.testepicpic.config.ConfigFirebase;
import com.example.testepicpic.helper.Permissao;
import com.example.testepicpic.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigPerfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigPerfilFragment extends Fragment {
    public String[] permissoesNecessarias = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private ImageButton imgbtnCamera, imgbtnGaleria;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA =200;
    private CircleImageView circleImageViewPerfil;
    private StorageReference storageReference;
    private String IdentificadorUsuario;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Object View;
    private StorageReference ConfiguracaoFirebase;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PerfilFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PerfilFragment newInstance(String param1, String param2) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ConfigPerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        imgbtnGaleria = view.findViewById(R.id.imgbtnGaleria);
        imgbtnCamera = view.findViewById(R.id.imgbtnCamera);
        circleImageViewPerfil = view.findViewById(R.id.circleImageViewPerfil);
        storageReference= ConfigFirebase.getFirebaseStorage();
        IdentificadorUsuario = Usuario.getIdentificadorUsuario();

        Permissao.validarpermisoes(permissoesNecessarias, getActivity(), 1);


        imgbtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_CAMERA);
                }
            }
        });

        imgbtnGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == RESULT_OK) {
            Bitmap imagem = null;
            try {
                switch (resultCode){

                    case SELECAO_CAMERA:
                        imagem= (Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem= MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),localImagemSelecionada);
                        break;
                }
                if (imagem != null){


                    circleImageViewPerfil.setImageBitmap(imagem);
                    //recuperar dados da imagem no Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //salvando no firebase
                    StorageReference imagemRef = storageReference.child("fotoperfil")
                            .child(IdentificadorUsuario+ ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getActivity(), "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResult: grantResults){
            if (permissaoResult == PackageManager.PERMISSION_DENIED){

            }
        }
    }
    private void alertaVlidarPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar essa função, aceite as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}

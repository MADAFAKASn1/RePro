package com.example.repro.ui.videos;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repro.databinding.FragmentVideosBinding;
import com.example.repro.ui.adaptadores.VideosAdapter;
import com.example.repro.ui.modelo.Video;

import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends Fragment {

    private FragmentVideosBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private RecyclerView recyclerView;
    private List<Video> videoList;
    private VideosAdapter adaptadorVideos;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVideosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerViewVideos;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        videoList = new ArrayList<>();

        adaptadorVideos = new VideosAdapter(videoList, getContext());
        recyclerView.setAdapter(adaptadorVideos);

        checkAndRequestPermissions();

        return root;
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13+
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_MEDIA_VIDEO}, PERMISSION_REQUEST_CODE);
            } else {
                loadVideos();
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(getContext(), "El permiso es necesario para cargar los videos.", Toast.LENGTH_LONG).show();
                }
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                loadVideos();
            }
        }
    }

    private void loadVideos() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA
        };
        Cursor videoCursor = contentResolver.query(videoUri, projection, null, null, null);

        if (videoCursor != null && videoCursor.moveToFirst()) {
            int idColumn = videoCursor.getColumnIndex(MediaStore.Video.Media._ID);
            int titleColumn = videoCursor.getColumnIndex(MediaStore.Video.Media.TITLE);
            int uriColumn = videoCursor.getColumnIndex(MediaStore.Video.Media.DATA);

            do {
                long id = videoCursor.getLong(idColumn);
                String title = videoCursor.getString(titleColumn);
                String uri = videoCursor.getString(uriColumn);
                videoList.add(new Video(title, uri));
            } while (videoCursor.moveToNext());

            videoCursor.close();
            adaptadorVideos.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVideos();
            } else {
                Toast.makeText(getContext(), "Permiso denegado.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.fileexplorer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.fileexplorer.Fragments.HomeFragment;
import com.example.fileexplorer.Fragments.InternalStorageFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);


        setSupportActionBar(toolbar);

        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open_Drawer, R.string.Close_Drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        HomeFragment homeFragment = new HomeFragment();
        fragmentTransaction.replace(R.id.frame_layout, homeFragment);
        fragmentTransaction.commit();


    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()){
            case R.id.nav_home:

                HomeFragment homeFragment = new HomeFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,
                        homeFragment).addToBackStack(null).commit();

                break;

            case R.id.nav_internal:

                InternalStorageFragment internalStorageFragment = new InternalStorageFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,
                        internalStorageFragment).addToBackStack(null).commit();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}
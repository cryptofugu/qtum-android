package org.qtum.mromanovsky.qtum.ui.fragment.WalletFragment;


import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import org.qtum.mromanovsky.qtum.dataprovider.UpdateData;
import org.qtum.mromanovsky.qtum.dataprovider.UpdateService;
import org.qtum.mromanovsky.qtum.datastorage.QtumSharedPreference;
import org.qtum.mromanovsky.qtum.ui.activity.MainActivity.MainActivity;
import org.qtum.mromanovsky.qtum.ui.fragment.BaseFragment.BaseFragmentPresenterImpl;
import org.qtum.mromanovsky.qtum.ui.fragment.ReceiveFragment.ReceiveFragment;
import org.qtum.mromanovsky.qtum.ui.fragment.SendBaseFragment.SendBaseFragment;
import org.qtum.mromanovsky.qtum.ui.fragment.TransactionFragment.TransactionFragment;


class WalletFragmentPresenterImpl extends BaseFragmentPresenterImpl implements WalletFragmentPresenter {

    Intent mIntent;
    UpdateService mUpdateService;

    private WalletFragmentInteractorImpl mWalletFragmentInteractor;
    private WalletFragmentView mWalletFragmentView;

    WalletFragmentPresenterImpl(WalletFragmentView walletFragmentView) {
        mWalletFragmentView = walletFragmentView;
        mWalletFragmentInteractor = new WalletFragmentInteractorImpl(getView().getContext());
    }

    //Service
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mUpdateService = ((UpdateService.UpdateBinder) iBinder).getService();
            if(mUpdateService.isMonitoring()){
                mUpdateService.unsubscribe();
            }
            mUpdateService.registerListener(new UpdateData() {
                @Override
                public void updateDate() {
                    mUpdateService.unsubscribe();
                }
            });
            mUpdateService.sendDefaultNotification();
            //mUpdateService.registerListener(mUpdateData);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onCreate(Context context) {
        super.onCreate(context);
        ((MainActivity)getView().getFragmentActivity()).getBottomNavigationView().getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        loadAndUpdateData();
        loadAndUpdateBalance();

        String pubKey = getInteractor().getAddress();
        getView().updatePubKey(pubKey);
    }

    @Override
    public void onStart(Context context) {
        super.onStart(context);

        //Service
        mIntent = new Intent(context, UpdateService.class);
        if(!isMyServiceRunning(UpdateService.class)) {
            context.startService(mIntent);
        }


        //context.bindService(mIntent,mServiceConnection,Context.BIND_AUTO_CREATE);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getView().getFragmentActivity().getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume(Context context) {
        super.onResume(context);
        context.bindService(mIntent,mServiceConnection,0);
    }

    @Override
    public void onPause(Context context) {
        super.onPause(context);
        mUpdateService.startMonitoringHistory();
    }

    @Override
    public void onStop(Context context) {
        super.onStop(context);
        context.unbindService(mServiceConnection);


        //Service
        //context.unbindService(mServiceConnection);

    }

    //Service
    UpdateData mUpdateData = new UpdateData() {
        @Override
        public void updateDate() {

        }
    };

    @Override
    public WalletFragmentView getView() {
        return mWalletFragmentView;
    }

    public WalletFragmentInteractorImpl getInteractor() {
        return mWalletFragmentInteractor;
    }

    @Override
    public void onClickReceive() {
        Fragment fragment = ReceiveFragment.newInstance();
        getView().openFragmentAndAddToBackStack(fragment);
    }

    @Override
    public void onClickQrCode() {
        SendBaseFragment sendBaseFragment = SendBaseFragment.newInstance(true);
        getView().openFragment(sendBaseFragment);
        ((MainActivity)getView().getFragmentActivity()).getBottomNavigationView().getMenu().getItem(3).setChecked(true);
    }

    @Override
    public void onRefresh() {
        loadAndUpdateData();
        loadAndUpdateBalance();
    }

    @Override
    public void sharePubKey() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "My QTUM address: " + getInteractor().getAddress());
        emailIntent.setType("text/plain");
        getView().getFragmentActivity().startActivity(emailIntent);
    }

    @Override
    public void openTransactionFragment(int position) {
        Fragment fragment = TransactionFragment.newInstance(position);
        getView().openFragmentAndAddToBackStack(fragment);
    }

    @Override
    public void initializeViews() {
        super.initializeViews();
        updateData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getInteractor().unSubscribe();
        getView().setAdapterNull();
    }

    private void loadAndUpdateData(){
        getView().startRefreshAnimation();
        getInteractor().getHistoryList(new WalletFragmentInteractorImpl.GetHistoryListCallBack() {
            @Override
            public void onSuccess() {
                updateData();
            }

            @Override
            public void onSuccessWithoutChange() {
                getView().stopRefreshRecyclerAnimation();
            }

            @Override
            public void onError(Throwable e) {
                getView().stopRefreshRecyclerAnimation();
                Toast.makeText(getView().getContext(),e.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAndUpdateBalance(){
        getInteractor().getBalance(new WalletFragmentInteractorImpl.GetBalanceCallBack() {
            @Override
            public void onSuccess(double balance) {
                getView().updateBalance(balance * (QtumSharedPreference.getInstance().getExchangeRates(getView().getContext())));
            }
        });
    }

    private void updateData(){
        getView().updateRecyclerView(getInteractor().getHistoryList());
    }
}

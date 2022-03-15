package com.alphawallet.app.viewmodel.walletconnect;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.alphawallet.app.R;
import com.alphawallet.app.entity.Operation;
import com.alphawallet.app.entity.SignAuthenticationCallback;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.entity.cryptokeys.SignatureFromKey;
import com.alphawallet.app.interact.FetchWalletsInteract;
import com.alphawallet.app.repository.TransactionRepositoryType;
import com.alphawallet.app.service.AWWalletConnectClient;
import com.alphawallet.app.service.KeyService;
import com.alphawallet.app.viewmodel.BaseViewModel;
import com.alphawallet.token.entity.Signable;
import com.alphawallet.token.tools.Numeric;
import com.walletconnect.walletconnectv2.client.WalletConnect;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Single;
import timber.log.Timber;

import static com.alphawallet.app.entity.cryptokeys.SignatureReturnType.SIGNATURE_GENERATED;

@HiltViewModel
public class SignMethodDialogViewModel extends BaseViewModel
{
    private AWWalletConnectClient awWalletConnectClient;
    private FetchWalletsInteract fetchWalletsInteract;
    private TransactionRepositoryType transactionRepositoryType;
    private KeyService keyService;
    private MutableLiveData<Boolean> completed = new MutableLiveData<>(false);

    @Inject
    public SignMethodDialogViewModel(AWWalletConnectClient awWalletConnectClient, FetchWalletsInteract fetchWalletsInteract, TransactionRepositoryType transactionRepositoryType, KeyService keyService)
    {
        this.awWalletConnectClient = awWalletConnectClient;
        this.fetchWalletsInteract = fetchWalletsInteract;
        this.transactionRepositoryType = transactionRepositoryType;
        this.keyService = keyService;
    }

    public void sign(Activity activity, Wallet wallet, WalletConnect.Model.SessionRequest sessionRequest, final Signable signable)
    {
        keyService.getAuthenticationForSignature(wallet, activity, new SignAuthenticationCallback()
        {
            @Override
            public void gotAuthorisation(boolean gotAuth)
            {
                Log.d("seaborn", "gotAuthorisation: " + gotAuth);
                if (gotAuth)
                {
                    long chainId = Long.parseLong(sessionRequest.getChainId().split(":")[1]);
                    Single<SignatureFromKey> signature = transactionRepositoryType.getSignature(wallet, signable, chainId);
                    signature
                            .delay(5, TimeUnit.SECONDS) // The WC connection shutdown when show biometric, when back to foreground, it will open new connection, so need delay to wait the connection opened
                            .subscribe(signatureFromKey -> onSuccess(signatureFromKey, sessionRequest), SignMethodDialogViewModel.this::onError);
                }
                else {
                    Toast.makeText(activity, activity.getString(R.string.error_while_signing_transaction), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void cancelAuthentication()
            {
                Timber.d("cancelAuthentication");
            }
        });
    }

    public void onError(Throwable throwable)
    {
        Timber.tag("seaborn").e(throwable);
    }

    private void onSuccess(SignatureFromKey signatureFromKey, WalletConnect.Model.SessionRequest sessionRequest)
    {
        Log.d("seaborn", "signed:" + signatureFromKey.sigType);
        if (signatureFromKey.sigType == SIGNATURE_GENERATED)
        {
            String result = Numeric.toHexString(signatureFromKey.signature);
            awWalletConnectClient.approve(sessionRequest, result);
        } else
        {
            Log.d("seaborn", "sign fail: " + signatureFromKey.failMessage);
            awWalletConnectClient.reject(sessionRequest, signatureFromKey.failMessage);
        }
        completed.postValue(true);
    }

    public void completeAuthentication(Operation taskCode)
    {
        keyService.completeAuthentication(taskCode);
    }

    public void failedAuthentication(Operation taskCode)
    {
        keyService.failedAuthentication(taskCode);
    }

    public LiveData<Boolean> completed()
    {
        return completed;
    }

    public void reject(WalletConnect.Model.SessionRequest sessionRequest)
    {

        awWalletConnectClient.reject(sessionRequest);
    }

    public Single<Wallet> findWallet(String walletAddress)
    {
        return fetchWalletsInteract.getWallet(walletAddress);
    }
}
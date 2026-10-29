package com.lazygarden.game

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.unity3d.ads.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.*

/** LazyGarden-only ad configuration. Rewarded is opt-in and each show owns one durable, one-use receipt. */
class RewardedVideos(private val activity:Activity,private val save:GardenSave){
 companion object {
  const val UNITY_GAME="6214767";const val UNITY_REWARDED="Rewarded_Android"
  const val UNITY_INTERSTITIAL="Interstitial_Android";const val UNITY_BANNER="Banner_Android"
  private const val ADMOB_TEST_REWARDED="ca-app-pub-3940256099942544/5224354917"
 }
 private val main=Handler(Looper.getMainLooper());private var busy=false;private var released=false;private var generation=0
 private var receipt:String?=null;private var callback:((Boolean,String)->Unit)?=null
 private var earned=false
 private var unityGate=RewardCompletionGate(UNITY_REWARDED)
 private val ledger=AdTestLedger(activity.applicationContext)
 private var activeProvider=""
 private val timeout=Runnable{finish(false,"Videoja nuk u ngarkua. Provo përsëri më vonë.")}
 fun show(kind:String,done:(Boolean,String)->Unit){
  if(released||busy||activity.isFinishing||save.constructing){done(false,"Videoja nuk është gati.");return}
  receipt=save.beginAdReward(kind);if(receipt==null){done(false,"Bonusi nuk mund të hapet tani.");return};busy=true;earned=false;activeProvider="";unityGate=RewardCompletionGate(UNITY_REWARDED);callback=done;generation++;val request=generation;main.postDelayed(timeout,45000)
  if(save.nextRewardedProvider()=="admob"&&BuildConfig.ADMOB_CONFIGURED)admob(request,true) else unity(request,true)
 }
 private fun unity(request:Int,allowFallback:Boolean){
  activeProvider="unity";ledger.attempt(activeProvider)
  fun load(){if(valid(request))UnityAds.load(UNITY_REWARDED,object:IUnityAdsLoadListener{
   override fun onUnityAdsAdLoaded(id:String){main.post{if(valid(request)&&id==UNITY_REWARDED){main.removeCallbacks(timeout);UnityAds.show(activity,id,UnityAdsShowOptions(),object:IUnityAdsShowListener{
    override fun onUnityAdsShowStart(id:String){}
    override fun onUnityAdsShowClick(id:String){}
    override fun onUnityAdsShowFailure(id:String,error:UnityAds.UnityAdsShowError,message:String){main.post{if(valid(request))if(allowFallback)admob(request,false) else finish(false,"Videoja dështoi. Nuk u dha bonus.")}}
    override fun onUnityAdsShowComplete(id:String,state:UnityAds.UnityAdsShowCompletionState){main.post{if(valid(request))finish(unityGate.complete(id,state==UnityAds.UnityAdsShowCompletionState.COMPLETED),"Videoja u mbyll pa bonus.")}}
   })}}}
   override fun onUnityAdsFailedToLoad(id:String,error:UnityAds.UnityAdsLoadError,message:String){main.post{if(valid(request))if(allowFallback)admob(request,false) else finish(false,"Videoja nuk është në dispozicion. Nuk u dha bonus.")}}
  })}
  if(UnityAds.isInitialized)load() else UnityAds.initialize(activity.applicationContext,UNITY_GAME,BuildConfig.ADS_TEST_MODE,object:IUnityAdsInitializationListener{
   override fun onInitializationComplete(){main.post{load()}}
   override fun onInitializationFailed(error:UnityAds.UnityAdsInitializationError,message:String){main.post{if(valid(request))if(allowFallback)admob(request,false) else finish(false,"Videoja nuk është në dispozicion. Nuk u dha bonus.")}}
  })
 }
 private fun valid(request:Int)=!released&&busy&&generation==request&&!activity.isFinishing&&!activity.isDestroyed
 private fun admob(request:Int,allowFallback:Boolean){
  if(!BuildConfig.ADMOB_CONFIGURED){if(allowFallback)unity(request,false) else finish(false,"Nuk ka video të disponueshme tani. Nuk u dha bonus.");return}
  val unitId=if(BuildConfig.ADS_TEST_MODE)ADMOB_TEST_REWARDED else BuildConfig.ADMOB_REWARDED_ID
  if(unitId.isBlank()){if(allowFallback)unity(request,false) else finish(false,"AdMob Rewarded nuk është konfiguruar. Nuk u dha bonus.");return}
  activeProvider="admob";ledger.attempt(activeProvider)
  MobileAds.initialize(activity){main.post{if(valid(request))RewardedAd.load(activity,unitId,AdRequest.Builder().build(),object:RewardedAdLoadCallback(){
   override fun onAdFailedToLoad(error:LoadAdError){if(valid(request))if(allowFallback)unity(request,false) else finish(false,"Videoja nuk është në dispozicion. Nuk u dha bonus.")}
   override fun onAdLoaded(ad:RewardedAd){if(!valid(request))return;main.removeCallbacks(timeout);ad.fullScreenContentCallback=object:FullScreenContentCallback(){
    override fun onAdDismissedFullScreenContent(){if(valid(request))finish(earned,"Videoja u mbyll pa bonus.")}
    override fun onAdFailedToShowFullScreenContent(error:AdError){if(valid(request))if(allowFallback)unity(request,false) else finish(false,"Videoja nuk u hap. Nuk u dha bonus.")}
   };ad.show(activity){if(valid(request)&&!earned){earned=receipt?.let{save.settleAdReward(it)}==true}}
   }
  })}}
 }
 private fun finish(completed:Boolean,failure:String){if(!busy)return;main.removeCallbacks(timeout);val id=receipt;val rewarded=if(earned)true else completed&&id!=null&&save.settleAdReward(id);if(rewarded&&activeProvider.isNotBlank())ledger.completed(activeProvider);if(!rewarded&&id!=null)save.cancelAdReward(id);val done=callback;callback=null;busy=false;receipt=null;activeProvider="";done?.invoke(rewarded,if(rewarded)"Bonusi u ruajt!" else failure)}
 fun cancel(){unityGate.cancel();generation++;finish(false,"Videoja u anulua. Nuk u dha bonus.")}
 fun release(){released=true;generation++;main.removeCallbacksAndMessages(null);receipt?.let{save.cancelAdReward(it)};callback=null;receipt=null;activeProvider="";busy=false}
}

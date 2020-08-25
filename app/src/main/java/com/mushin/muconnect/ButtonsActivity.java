package com.mushin.muconnect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class ButtonsActivity extends DialogFragment {

    private static final String LOG_TAG = "SANA_BLE";
    private static final int REQUEST_FILE_SELECT = 3;

	public static boolean AutoSessionOn = false;

    private void showMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private Dialog dialog;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.activity_buttons, null);
        builder.setView(view);
        dialog = builder.create();
//        dialog.setCanceledOnTouchOutside(false);

        initActionsList(view);

        // hack to enable soft keyboard
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        initActionsList();
//    }

    private void initActionsList(View view) {
//        setContentView(R.layout.activity_buttons);

        final MainActivity activity = (MainActivity) getActivity();

        final ListView actionsList = view.findViewById(R.id.actionsList);
        final ActionListAdapter adapter = new ActionListAdapter(getActivity());

//        actionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                actionsList.setItemChecked(position, true);
//                adapter.setSelectedPosition(position);
//                adapter.notifyDataSetChanged();
//            }
//        });

        // Receive session data
        adapter.add(new TwoParamActionItemModel(
                "Receive Session Data",
                "First Session ID:",
                "Last Session ID:",
                new TwoParamActionItemModel.Executor() {
                    @Override
                    public void execute(String str1, String str2) {

                        showMessage("Requesting Data From Sana Mask");

                        int fromSession = Integer.parseInt(str1);
                        int toSession = Integer.parseInt(str2);
                        ((MainActivity)getActivity()).initiateSendData(fromSession, toSession);

                        Log.e(LOG_TAG," *** Deleting "+com.mushin.muconnect.MainActivity.FirstFileName);

			if(com.mushin.muconnect.MainActivity.FirstSanaFile == null)
			{
				Log.e(LOG_TAG, "Nothing to delete");
			}
			else
			{
				if(com.mushin.muconnect.MainActivity.FirstSanaFile.exists())
				{
					com.mushin.muconnect.MainActivity.FirstSanaFile.delete();
					if (com.mushin.muconnect.MainActivity.FirstSanaFile.exists())
					{
						Log.e(LOG_TAG, "*** [A] " + com.mushin.muconnect.MainActivity.FirstFileName + "didn't get deleted");
					}
				} 
				else 
				{
				Log.e(LOG_TAG, "*** [A] " + com.mushin.muconnect.MainActivity.FirstFileName + "didn't exist; Nothing to delete");
				}
			}

                        dialog.dismiss();
                    }
                },
                new TwoParamActionItemModel.Validator() {
                    @Override
                    public boolean validate(String str1, String str2) {
                        boolean result = false;
                        if (str1 != null && !str1.isEmpty() && str2 != null && !str2.isEmpty() ) {
                            try {
                                int param1 = Integer.parseInt(str1);
                                int param2 = Integer.parseInt(str2);

                                result = (param1 >= 0) && (param2 >= 0) && (param2 >= param1);
                            } catch (NumberFormatException ex) {
                                result = false;
                            }
                        }
                        return result;
                    }
                }
        ));

		 // Receive session data
        adapter.add(new TwoParamActionItemModel(
                "Receive Session Log",
                "First Session ID:",
                "Last Session ID:",
                new TwoParamActionItemModel.Executor() {
                    @Override
                    public void execute(String str1, String str2) {

                        showMessage("Requesting Log From Sana Mask");

                        int fromSession = Integer.parseInt(str1);
                        int toSession = Integer.parseInt(str2);
                        ((MainActivity)getActivity()).initiateSendLog(fromSession, toSession);

                        Log.e(LOG_TAG," *** Deleting "+com.mushin.muconnect.MainActivity.FirstFileName);

			if(com.mushin.muconnect.MainActivity.FirstSanaFile == null)
			{
				Log.e(LOG_TAG, "Nothing to delete");
			}
			else
			{
				if(com.mushin.muconnect.MainActivity.FirstSanaFile.exists())
				{
					com.mushin.muconnect.MainActivity.FirstSanaFile.delete();
					if (com.mushin.muconnect.MainActivity.FirstSanaFile.exists())
					{
						Log.e(LOG_TAG, "*** [A] " + com.mushin.muconnect.MainActivity.FirstFileName + "didn't get deleted");
					}
				} 
				else 
				{
				Log.e(LOG_TAG, "*** [A] " + com.mushin.muconnect.MainActivity.FirstFileName + "didn't exist; Nothing to delete");
				}
			}

                        dialog.dismiss();
                    }
                },
                new TwoParamActionItemModel.Validator() {
                    @Override
                    public boolean validate(String str1, String str2) {
                        boolean result = false;
                        if (str1 != null && !str1.isEmpty() && str2 != null && !str2.isEmpty() ) {
                            try {
                                int param1 = Integer.parseInt(str1);
                                int param2 = Integer.parseInt(str2);

                                result = (param1 >= 0) && (param2 >= 0) && (param2 >= param1);
                            } catch (NumberFormatException ex) {
                                result = false;
                            }
                        }
                        return result;
                    }
                }
        ));

        adapter.add(new SeparatorItemModel());

        // Erase session data
        adapter.add(new SimpleActionItemModel("Erase All Session Data", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "EraseData";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

	 // Erase Error Log 
        adapter.add(new SimpleActionItemModel("Erase All Event Log Data", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "EraseLog";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

        adapter.add(new SeparatorItemModel());

        // Store Mode to Flash and Reboot
        adapter.add(new SimpleActionItemModel("Store Mode To Flash and Reboot", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "StoreMODE";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

        // Enable Stored Mode
        adapter.add(new SimpleActionItemModel("Enable Stored Mode", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "EnableMODE";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

	// Disable Stored Mode
	adapter.add(new SimpleActionItemModel("Disable Stored Mode", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "DisableMODE";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

	// Set Mask to Secure BLE Mode
        adapter.add(new SimpleActionItemModel("Set Mask to Secure BLE Mode", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                // We need to reverse this because of a bug in the firmware
                //String message = "SetSECBLEMODE";
		  String message = "SetSTDBLEMODE";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

	 // Set Mask to Standard BLE Mode
        adapter.add(new SimpleActionItemModel("Set Mask to Standard BLE Mode", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
            // We need to reverse this because of a bug in the firmware
            //    String message = "SetSTDBLEMODE";
			String message = "SetSECBLEMODE";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

	  // Set Mask to No-App Mode
        adapter.add(new SimpleActionItemModel("Set Mask to No-App Mode", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "SetNOAPPMODE";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

        // Set Mask to App Mode
        adapter.add(new SimpleActionItemModel("Set Mask to App Mode", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "SetAPPMODE";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

	 // Set Mask to Real Mode
        adapter.add(new SimpleActionItemModel("Set Mask to Real Mode", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "SetREALMODE";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

        // Set Mask to Sham Mode
        adapter.add(new SimpleActionItemModel("Set Mask to Sham Mode", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "SetSHAMMODE";

                writeMessage(message);

                dialog.dismiss();

            }
        }));

        adapter.add(new SeparatorItemModel());

        // Reset Volume and Light
        adapter.add(new SimpleActionItemModel("Reset Volume and Light", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "ResetVL";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

        adapter.add(new SeparatorItemModel());

        // Start Session
        adapter.add(new SimpleActionItemModel("Start Session", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "StartSession";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

        // Stop Session
        adapter.add(new SimpleActionItemModel("Stop Session", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "StopSession";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

        // Pause Session
        adapter.add(new SimpleActionItemModel("Pause Session", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "PauseSession";

                writeMessage(message);

                dialog.dismiss();
            }
        }));

	 // Start Auto-Session
        adapter.add(new SimpleActionItemModel("Auto Session", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
			String message = "StartSession";

			AutoSessionOn = true;

			writeMessage(message);

			dialog.dismiss();
            }
        }));

        // Resume Session
        adapter.add(new SimpleActionItemModel("Send Parameters", new SimpleActionItemModel.Executor() {
            @Override
            public void execute() {
                String message = "SendParameters";

                writeMessage(message);

                dialog.dismiss();
            }
        }));


        actionsList.setAdapter(adapter);
    }

    static boolean writeMessage(String message) {
        byte[] value;
        boolean result = false;
        try
        {
            // send data to service
            value = message.getBytes("UTF-8");

            if( com.mushin.muconnect.MainActivity.mService != null) com.mushin.muconnect.MainActivity.mService.writeRXCharacteristic(value);

            Log.e(LOG_TAG, "*** Sending: ".concat(message));

            result = true;
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }
}

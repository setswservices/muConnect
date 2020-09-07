package com.mushin.muconnect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

abstract class ListItemModelBase {

    public enum Type {
        SEPARATOR(0),
        SIMPLE_ACTION (1),
        ACTION_WITH_ONE_PARAM (2),
        ACTION_WITH_TWO_PARAMS (3);

        private int value;
        Type(int value) {
            this.value = value;
        }

        public int getValue() { return value; }
    }

    private Type viewType;
    private String title;
    private int position = -1;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public ListItemModelBase(Type type) {
        this.viewType = type;
    }

    public Type getViewType() {
        return viewType;
    }

    public abstract boolean canExecute();
    public abstract void updateView(ViewGroup parent, final View view, boolean isSelected);

    static void hideKeyboard(final View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

class SeparatorItemModel extends ListItemModelBase {

    public SeparatorItemModel() {
        super(Type.SEPARATOR);
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public void updateView(ViewGroup parent, View view, boolean isSelected) {
    }
}

class SimpleActionItemModel extends ListItemModelBase {

    public interface Executor {
        void execute();
    }
    private final Executor executor;

    public SimpleActionItemModel(String title, Executor executor) {
        super(Type.SIMPLE_ACTION);

        this.setTitle(title);
        this.executor = executor;
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void updateView(ViewGroup parent, final View view, boolean isSelected) {

        final Button btnRun = view.findViewById(R.id.itemRunButton);

        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int w = view.getWidth();
                    btnRun.setWidth(w / 2);
                }
            });
        }
        btnRun.setText(this.getTitle());
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executor.execute();
            }
        });
    }
}

class TwoParamActionItemModel extends ListItemModelBase {
    private String paramTitle1;
    private String paramTitle2;
    private String param1;
    private String param2;
    private final Validator validator;
    private final Executor executor;

    public interface Validator {
        boolean validate(String str1, String str2);
    }

    public interface Executor {
        void execute(String str1, String str2);
    }

    public TwoParamActionItemModel(String title, String paramTitle1, String paramTitle2, Executor executor, Validator validator) {
        super(Type.ACTION_WITH_TWO_PARAMS);
        this.executor = executor;
        this.validator = validator;

        this.setTitle(title);

        this.paramTitle1 = paramTitle1;
        this.paramTitle2 = paramTitle2;

        this.param1 = "";
        this.param2 = "";
    }

    public void setParamTitle1(String paramTitle1) {
        this.paramTitle1 = paramTitle1;
    }
    public void setParamTitle2(String paramTitle2) {
        this.paramTitle2 = paramTitle2;
    }
    public void setParam1(String value) {
        this.param1 = value;
    }
    public void setParam2(String value) {
        this.param2 = value;
    }

    @Override
    public boolean canExecute() {
        if (validator == null) {
            return true;
        }
        return validator.validate(param1, param2);
    }

    @Override
    public void updateView(final ViewGroup parent, final View view, boolean isSelected) {
        TextView title = view.findViewById(R.id.itemTitle);
        title.setText(this.getTitle());
        title.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        final Button btnShow = view.findViewById(R.id.itemShowButton);
        btnShow.setText(this.getTitle());
        btnShow.setVisibility(!isSelected ? View.VISIBLE : View.GONE);
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int w = view.getWidth();
                    btnShow.setWidth(w / 2);
                }
            });
        }

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ListView actionsList = (ListView) parent;
                actionsList.setItemChecked(getPosition(), true);
                ActionListAdapter adapter = (ActionListAdapter) actionsList.getAdapter();
                adapter.setSelectedPosition(getPosition());
                adapter.notifyDataSetChanged();
            }
        });

        final Button btnHide = view.findViewById(R.id.itemCloseButton);
        btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ListView actionsList = (ListView) parent;
                actionsList.setItemChecked(getPosition(), false);
                ActionListAdapter adapter = (ActionListAdapter) actionsList.getAdapter();
                adapter.setSelectedPosition(-1);
                adapter.notifyDataSetChanged();

                hideKeyboard(view);
            }
        });
        btnHide.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        final Button btnRun = view.findViewById(R.id.itemRunButton);
        btnRun.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);

                executor.execute(param1, param2);
            }
        });

        View parametersRow1 = view.findViewById(R.id.parameters_row1);
        parametersRow1.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        View parametersRow2 = view.findViewById(R.id.parameters_row2);
        parametersRow2.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        TextView paramTitle1 = view.findViewById(R.id.parameter_title_1);
        paramTitle1.setText(this.paramTitle1);

        TextView paramTitle2 = view.findViewById(R.id.parameter_title_2);
        paramTitle2.setText(this.paramTitle2);

        final EditText txtParam1 = view.findViewById(R.id.parameter_value_1);
        final EditText txtParam2 = view.findViewById(R.id.parameter_value_2);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setParam1(txtParam1.getText().toString());
                setParam2(txtParam2.getText().toString());

                btnRun.setEnabled(canExecute());
            }
        };

        txtParam1.addTextChangedListener(watcher);
        txtParam2.addTextChangedListener(watcher);
    }
}

class ActionListAdapter extends ArrayAdapter<ListItemModelBase> {
    private final LayoutInflater mInflater;

    private int selectedPosition = -1;

    public ActionListAdapter(Context context) {
        super(context, -1);

        mInflater = LayoutInflater.from(context);
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }

    @Override
    public int getViewTypeCount() {
        return ListItemModelBase.Type.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        // important method so that convertView has a correct type
        return this.getItem(position).getViewType().getValue();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final ListItemModelBase item = this.getItem(position);
        ListItemModelBase.Type viewType = item.getViewType();

        if (convertView == null) {
            int layoutId = -1;
            switch(viewType) {
                case SEPARATOR:
                    layoutId = R.layout.action_view_separator;
                    break;
                case SIMPLE_ACTION:
                    layoutId = R.layout.action_view_0;
                    break;
//                case ACTION_WITH_ONE_PARAM:
//                    layoutId = R.layout.action_view_1;
//                    break;
                case ACTION_WITH_TWO_PARAMS:
                    layoutId = R.layout.action_view_2;
                    break;

            }
            convertView = this.mInflater.inflate(layoutId, null);
            item.setPosition(position);
        }

        item.updateView(parent, convertView, selectedPosition == position);
        convertView.setBackgroundColor(selectedPosition == position ? Color.parseColor("#eaeaea") : convertView.getResources().getColor(android.R.color.transparent));

        return convertView;
    }

}
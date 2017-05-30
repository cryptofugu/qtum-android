package com.pixelplex.qtum.ui.fragment.ContractConfirmFragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.pixelplex.qtum.R;
import com.pixelplex.qtum.dataprovider.RestAPI.gsonmodels.Contract.ContractMethodParameter;
import com.pixelplex.qtum.utils.FontTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kirillvolkov on 26.05.17.
 */

public class ContractConfirmViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.name)
    FontTextView name;

    @BindView(R.id.value)
    FontTextView value;

    @BindView(R.id.root_layout)
    RelativeLayout rootLayout;

    public ContractConfirmViewHolder(View itemView, final OnValueClick clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        rootLayout.setClickable(true);

        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(getAdapterPosition());
            }
        });
    }

    public void bind(ContractMethodParameter parameter) {
        name.setText(parameter.displayName);
        value.setText(parameter.value);
    }
}

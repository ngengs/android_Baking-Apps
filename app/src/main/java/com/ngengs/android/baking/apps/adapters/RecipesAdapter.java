/*******************************************************************************
 * Copyright (c) 2017 Rizky Kharisma (@ngengs)
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package com.ngengs.android.baking.apps.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ngengs.android.baking.apps.R;
import com.ngengs.android.baking.apps.data.Recipe;
import com.ngengs.android.baking.apps.remotes.Connection;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.ViewHolder> {

    private final List<Recipe> mData;
    private final Context mContext;
    private final OnClickListener mClickListener;

    /**
     * Recycler View adapter for recipes list.
     *
     * @param mContext
     *         Application or activity context
     * @param mClickListener
     *         Item click listener
     */
    public RecipesAdapter(Context mContext, OnClickListener mClickListener) {
        this.mContext = mContext;
        this.mData = new ArrayList<>();
        this.mClickListener = mClickListener;
    }

    @Override
    public RecipesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_recipes, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecipesAdapter.ViewHolder holder, int position) {
        Recipe recipe = mData.get(position);
        String recipeName = recipe.getName();
        if (TextUtils.isEmpty(recipe.getImage())) {
            if (recipeName.replaceAll("\\s", "").equalsIgnoreCase("nutellapie")) {
                recipe.setImage(Connection.FALLBACK_IMAGE_NUTELLA_PIE);
            } else if (recipeName.replaceAll("\\s", "").equalsIgnoreCase("brownies")) {
                recipe.setImage(Connection.FALLBACK_IMAGE_BROWNIES);
            } else if (recipeName.replaceAll("\\s", "").equalsIgnoreCase("yellowcake")) {
                recipe.setImage(Connection.FALLBACK_IMAGE_YELLOW_CAKE);
            } else {
                recipe.setImage(Connection.FALLBACK_IMAGE_CHEESE_CAKE);
            }
            mData.set(position, recipe);
        }
        Glide.with(mContext).load(recipe.getImage()).thumbnail(0.05f).into(holder.mImageRecipe);
        holder.mTextRecipe.setText(recipeName);
        holder.mTextServing.setText(String.valueOf(recipe.getServings()));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Insert list of new data to adapter.
     *
     * @param data
     *         Recipes list
     */
    public void addData(List<Recipe> data) {
        if (data != null) {
            int position = mData.size();
            mData.addAll(data);
            notifyItemRangeInserted(position, data.size());
        } else {
            Timber.w("addData: %s", "Data empty");
        }
    }

    public List<Recipe> getData() {
        return new ArrayList<>(mData);
    }

    public interface OnClickListener {
        void onClick(Recipe recipe);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mImageRecipe;
        private final TextView mTextRecipe;
        private final TextView mTextServing;

        ViewHolder(View view) {
            super(view);
            mImageRecipe = view.findViewById(R.id.image_recipe);
            mTextRecipe = view.findViewById(R.id.text_recipe);
            mTextServing = view.findViewById(R.id.text_serving);
            CardView mCardView = view.findViewById(R.id.card_view_recipe);
            mCardView.setOnClickListener(
                    itemClick -> mClickListener.onClick(mData.get(getAdapterPosition())));
        }
    }
}

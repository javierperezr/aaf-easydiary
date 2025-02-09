package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.createBackgroundGradientDrawable
import me.blog.korn123.commons.utils.EasyDiaryUtils.createThumbnailGlideOptions
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ItemDiaryDashboardBinding
import me.blog.korn123.easydiary.enums.Calculation
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
import me.blog.korn123.easydiary.helper.THUMBNAIL_BACKGROUND_ALPHA
import me.blog.korn123.easydiary.models.Diary

class DiaryDashboardItemAdapter(val activity: Activity) : BaseBannerAdapter<Diary>() {

    override fun bindData(
        holder: BaseViewHolder<Diary>,
        diary: Diary,
        position: Int,
        pageSize: Int
    ) {
        val context = holder.itemView.context
        val binding = ItemDiaryDashboardBinding.bind(holder.itemView)

        binding.root.run {
            context.updateTextColors(this, 0, 0)
            context.updateAppViews(this)
            context.initTextSize(this)
            context.updateCardViewPolicy(this)
            FontUtils.setFontsTypeface(context, null, this)
            binding.run {
                activity.applyMarkDownPolicy(textContents, diary.contents!!, false, arrayListOf(), true)
                textDateTime.text = when (diary.isAllDay) {
                    true -> DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis)
                    false -> DateUtils.getDateTimeStringFromTimeMillis(diary.currentTimeMillis)
                }

                context.run {
                    if (config.enableLocationInfo) {
                        diary.location?.let {
                            changeDrawableIconColor(config.primaryColor, R.drawable.ic_map_marker_2)
                            locationLabel.text = it.address
                            locationContainer.visibility = View.VISIBLE
                        } ?: run { locationContainer.visibility = View.GONE }
                    } else {
                        locationContainer.visibility = View.GONE
                    }

                    if (config.enableCountCharacters) {
                        contentsLength.run {
                            text = context.getString(R.string.diary_contents_length, diary.contents?.length ?: 0)
                        }
                        contentsLengthContainer.visibility = View.VISIBLE
                    } else {
                        contentsLengthContainer.visibility = View.GONE
                    }

                    FlavorUtils.initWeatherView(this, imageSymbol, diary.weather)

                    photoViews.removeAllViews()
                    if (diary.photoUris?.size ?: 0 > 0) {
                        diary.photoUrisWithEncryptionPolicy()?.map {
                            val imageXY = dpToPixel(32F)
                            val imageView = ImageView(this)
                            val layoutParams = LinearLayout.LayoutParams(imageXY, imageXY)
                            layoutParams.setMargins(0, dpToPixel(1F), dpToPixel(3F), 0)
                            imageView.layoutParams = layoutParams
                            imageView.background = createBackgroundGradientDrawable(config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA, imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL)
                            imageView.scaleType = ImageView.ScaleType.CENTER
                            dpToPixel(1.5F, Calculation.FLOOR).apply {
                                imageView.setPadding(this, this, this, this)
                            }
                            val listener = object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean { return false }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean { return false }
                            }
                            Glide.with(this).load(EasyDiaryUtils.getApplicationDataDirectory(this) + it.getFilePath())
                                .listener(listener)
                                .apply(createThumbnailGlideOptions(imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL, it.isEncrypt()))
                                .into(imageView)
                            photoViews.addView(imageView)
                        }
                    }

//                    textContents.maxLines = when (config.enableContentsSummary) {
//                        true -> config.summaryMaxLines
//                        false -> Integer.MAX_VALUE
//                    }
                }
            }
        }
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_diary_dashboard
    }

}
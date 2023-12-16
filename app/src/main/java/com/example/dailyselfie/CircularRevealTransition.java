package com.example.dailyselfie;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import androidx.transition.TransitionValues;
import androidx.transition.Visibility;

public class CircularRevealTransition extends Visibility {


    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        //Khởi tạo biến startRadius với giá trị 0
        int startRadius = 0;
        //Khởi tạo biến endRadius với giá trị bằng đường chéo của View
        int endRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
        //Tạo một đối tượng Animator có tên reveal bằng phương thức createCircularReveal của lớp ViewAnimationUtils
        Animator reveal = ViewAnimationUtils.createCircularReveal(view, view.getWidth() / 2, view.getHeight() / 2, startRadius, endRadius);
        //Thiết lập độ mờ của View thành 0
        view.setAlpha(0);
        //Thêm một listener cho animation reveal
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Khi animation bắt đầu, thiết lập độ mờ của View thành 1
                view.setAlpha(1);
            }
        });
        //Trả về đối tượng reveal để chạy animation
        return reveal;
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        int endRadius = 0;
        int startRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
        Animator reveal = ViewAnimationUtils.createCircularReveal(view, view.getWidth() / 2, view.getHeight() / 2, startRadius, endRadius);
        return reveal;
    }
}
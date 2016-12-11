package com.vchohan.cookbook;

/**
 * Created by vchohan on 12/10/16.
 */

public class RecipeUtils {

    private String image;

    private String title;




    public RecipeUtils () {

    }

    public RecipeUtils (String Image, String Title) {
        this.image = Image;
        this.title = Title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

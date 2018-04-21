# ArcView/ProgressView

Custom view to show process percentage.

  You can customize start and lenght of line, color, shadow, set weights and footer text. View will be scaled text inside depending on view size;
  
  More available ways to customize view described on class ArcView;
    
   <br />
  
  This is how view looks in common state:
 
  ![alt text](https://raw.githubusercontent.com/ikislenko93/ArcView/master/app/src/main/res/drawable/arc_view_example_one.png)
  
   <br />
   
   <b><com.example.i_kislenko.arcview.ArcView</b> 
   <b>android:id="@+id/arcView"</b> 
  
       
   <b>android:layout_width="wrap_content"</b> 
   <b>android:layout_height="wrap_content"</b> 

   <b>android:layout_alignParentStart="true"</b> 

   <b>app:footerText="OVERALL" /> </b> 
   
   <br />
   
   To set percentage you need call arcView.setPercentage(0.5f); in class and you will see 50% percentage;
   
   In sample project you can see all possible ways to customize this view.
   
  ![alt text](https://raw.githubusercontent.com/ikislenko93/ArcView/master/app/src/main/res/drawable/arc_view_example.png)
   

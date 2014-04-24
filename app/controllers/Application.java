package controllers;

import models.Extractor;
import play.*;
import play.data.Form;
import play.mvc.*;

import views.html.*;
import static play.data.Form.form;

public class Application extends Controller {

	public static class BlogUrl {
        public String url;
    } 
    
    // -- Actions
  
    /**
     * Home page
     */
    public static Result index() {
        return ok(
            index.render(form(BlogUrl.class))
        );
    }
    /**
     * Handles the form submission.
     */
    
    public static Result submit(int category){
    	Form<BlogUrl> form = form(BlogUrl.class).bindFromRequest();
    	if(form.hasErrors()) {
            return badRequest(index.render(form));
        } else {
        	BlogUrl bgrl = form.get();
        	return extractResult(category, bgrl.url);
        }
    }
    public static Result extractResult(int category,String url) {
//    	Form<BlogUrl> form = form(BlogUrl.class).bindFromRequest();
//    	if(form.hasErrors()) {
//            return badRequest(index.render(form));
//        } else {
//        	BlogUrl bgrl = form.get();
        	if(category == 0) {
        		String strTextOutput = Extractor.contentExtract(url);
                return ok(
                    results.render(url,strTextOutput)
                );
        	} else {
        		String strHtmlOutput = Extractor.textHtmlExtract(url);
//        		String strHtml = strHtmlOutput.substring(1, strHtmlOutput.length()-1);
                return ok(
                    results.render(url,strHtmlOutput)
                );
        	}
        	
            
//        }
    	
    
    }

}

package framework;

import java.util.HashMap;

public class ModelAndView {

    private String view;

    private HashMap<String,Object> data;


    public ModelAndView() {

        data = new HashMap<>();

    }


    public String getView() {
        return view;
    }


    public void setView(String view) {
        this.view = view;
    }


    public void addObject(String key, Object value) {

        data.put(key, value);

    }


    public HashMap<String,Object> getData() {

        return data;

    }

}
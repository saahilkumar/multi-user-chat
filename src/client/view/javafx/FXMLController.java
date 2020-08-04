package client.view.javafx;

import client.controller.Feature;
import javafx.scene.Scene;


public class FXMLController  {
  private Feature features;
  private Scene root;
  private String name;

  public void setFeatures(Feature features) {
    this.features = features;
    System.out.print("features:  " + this.features);
  }

}
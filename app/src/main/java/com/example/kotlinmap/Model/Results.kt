package com.example.kotlinmap.Model

class Results {

    /*public string business_status { get; set; }
  1  public Geometry geometry { get; set; }
  1  public string icon { get; set; }
  1  public string id { get; set; }
  1  public string name { get; set; }
  1  public OpeningHours opening_hours { get; set; }
  1  public IList<Photo> photos { get; set; }
  1  public string place_id { get; set; }
    public PlusCode plus_code { get; set; }
  1  public int price_level { get; set; }
  1  public double rating { get; set; }
  1  public string reference { get; set; }
  1  public string scope { get; set; }
  1  public IList<string> types { get; set; }
    public int user_ratings_total { get; set; }
  1  public string vicinity { get; set; }*/

    var business_status:String?=null
    var name:String?=null
    var icon:String?=null
    var geometry:Geometry?=null
    var plus_code:PlusCode?=null
    var opening_hours:OpeningHours?=null
    var photos:Array<Photos>?=null
    var types:Array<String>?=null
    var id:String?=null
    var place_id:String?=null
    var price_level:String?=null
    var rating:String?=null
    var reference:String?=null
    var scope:String?=null
    var user_ratings_total:String?=null
    var vicinity:String?=null

}
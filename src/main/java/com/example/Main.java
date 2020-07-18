/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }
  
  @RequestMapping("/generate")
  String generate(@RequestParam("longurl") String longurl, Map<String, Object> model){
	  
	  String shorturl = shorturls(save(longurl));
	  
	  shorturl = "https://andj.herokuapp.com/"+shorturl;
	  
	  model.put("shorturl", shorturl);
	  
	  
	  return "gen";
  }
  
  @RequestMapping("/{shortUrl}")
  public ResponseEntity<Void> getAndRedirect(@PathVariable String shortUrl){
	  String url = longUrl(shortUrl);
	  
	  return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
	  
  }
  
  String longUrl(String shortURL)
  {
	  int id = 0; // initialize result  
      
      // A simple base conversion logic  
      for (int i = 0; i < shortURL.length(); i++)  
      {  
          if ('a' <= shortURL.charAt(i) &&  
                     shortURL.charAt(i) <= 'z')  
          id = id * 62 + shortURL.charAt(i) - 'a';  
          if ('A' <= shortURL.charAt(i) &&  
                     shortURL.charAt(i) <= 'Z')  
          id = id * 62 + shortURL.charAt(i) - 'A' + 26;  
          if ('0' <= shortURL.charAt(i) &&  
                     shortURL.charAt(i) <= '9')  
          id = id * 62 + shortURL.charAt(i) - '0' + 52;  
      }  
      
      
      return myFun(id);
  }

  String shorturls(int n) {
	  char map[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();  
      
      StringBuffer shorturl = new StringBuffer();  
    
      while (n > 0)  
      {  
          shorturl.append(map[n % 62]); 
          n = n / 62;  
      }  
    
      return shorturl.reverse().toString();  

  }
  
  int save(String longurl) {
	  try (Connection connection = dataSource.getConnection()) {
		  Statement stmt = connection.createStatement();
		 
		  ResultSet rs = stmt.executeQuery("SELECT id FROM urls order by id desc limit 1");
		  int resultid = 0;
		  
		//  ArrayList<String> output = new ArrayList<String>();
	      while (rs.next()) {
	    	  
	    	  //output.add("Read from DB: " + rs.getString("url"));
	    	  resultid = rs.getInt("id");
	      }
	      
	      stmt.executeUpdate("INSERT INTO urls(url, shorturl) VALUES ('"+longurl+"', '"+shorturls(resultid+1)+"')");
	      return resultid+1;
	  }catch(Exception e) {
		  return 2;
	  }
	 // return 1;
  }
  
  String myFun(int id) {
	  String str="";
	  try (Connection connection = dataSource.getConnection()) {
		  Statement stmt = connection.createStatement();
		  ResultSet rs = stmt.executeQuery("SELECT url FROM urls where id="+id+"");
		  
			  
		  while(rs.next()) {
		  	str = rs.getString("url");
		  }
		  ResultSet fff = stmt.executeQuery("delete * FROM urls");
		  
	  }catch(Exception e) {
		  
	  }
	  
	  return str;
  }
  
//  @RequestMapping("/db")
//  String db(Map<String, Object> model) {
//    try (Connection connection = dataSource.getConnection()) {
//      Statement stmt = connection.createStatement();
//      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS urls (id serial primary key, url varchar(255), shorturl varchar(50))");
//    //  stmt.executeUpdate("INSERT INTO urls VALUES (4,'https://google.lc', 'asky.uz/4')");
//      ResultSet rs = stmt.executeQuery("SELECT url FROM urls");
//
//      ArrayList<String> output = new ArrayList<String>();
//      while (rs.next()) {
//        output.add("Read from DB: " + rs.getString("url"));
//      }
//
//      model.put("records", output);
//      return "db";
//    } catch (Exception e) {
//      model.put("message", e.getMessage());
//      return "error";
//    }
//  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}

package com.application.RetailHub.Entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
@Entity
@Table(name = "categories")
public class Category {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer category_id;

	    private String category_name;

	    @OneToMany(mappedBy = "category")
	    private List<Product> products;


	 public Category() {
			super();
		}

	 public Category(Integer category_id, String category_name, List<Product> products) {
		super();
		this.category_id = category_id;
		this.category_name = category_name;
		this.products = products;
	}

	 // getters & setters

		public Integer getCategory_id() {
			return category_id;
		}

		public void setCategory_id(Integer category_id) {
			this.category_id = category_id;
		}

		public String getCategory_name() {
			return category_name;
		}

		public void setCategory_name(String category_name) {
			this.category_name = category_name;
		}

		public List<Product> getProducts() {
			return products;
		}

		public void setProducts(List<Product> products) {
			this.products = products;
		}


	}
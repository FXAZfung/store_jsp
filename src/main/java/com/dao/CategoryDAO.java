package com.dao;

import java.util.ArrayList;

import com.bean.Category;

public interface CategoryDAO {
	public boolean add(Category category) throws Exception;
	public boolean update(Category category) throws Exception;
	public boolean delete(int cid) throws  Exception;//ɾ��
	public boolean deleteAll(String condition) throws  Exception;//ɾ������
	public Category getById(int cid) throws Exception;//����id��ȡ
	public Category getByName(String name) throws Exception;//����id��ȡ
	public ArrayList<Category> getAll() throws Exception; //��ѯ����
}

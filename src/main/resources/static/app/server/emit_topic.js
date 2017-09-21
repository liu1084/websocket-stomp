var amqp = require('../../node_modules/amqplib/callback_api');
var fs = require('fs');
var pushTimeout = 500;
amqp.connect('amqp://liujun:bbcchinese@localhost', function(err, conn) {
	if (err){
		console.error(err);
		return false;
	}

	conn.createChannel(function(err, ch) {
		if (err){
			setTimeout(function() { conn.close(); process.exit(0) }, 500);
		}

		setInterval(function(){
			pub(ch);
		}, pushTimeout);

		//pub(ch);
	});

	function pub(ch){
		var ex = 'amq.topic';
		var key = 'quakes.all';
		var msg = new Date().getTime().toString();
		var images = ['mobile1.jpg', 'mobile2.jpg', 'mobile3.jpg', 'mobile4.jpg', 'mobile5.jpg', 'mobile6.jpg'];
		for (var i = 0; i < images.length; i++){
			fs.readFile(images[i], function(err, original_data){
				if (err){
					console.log(err);
					return false;
				}
				var base64Image = new Buffer(original_data, 'binary').toString('base64');
				ch.assertExchange(ex, 'topic', {durable: true});
				ch.publish(ex, key, new Buffer(base64Image));
				report(key, base64Image);
			});

		}

	}

	function report(key, msg){
		console.log(" [x] Sent %s:'%s'", key, msg);
	}


});
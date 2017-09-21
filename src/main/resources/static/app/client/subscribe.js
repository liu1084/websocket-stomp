/**
 * Created by jim on 2017/6/23.
 *
 */
$(function(){

	var WS = function(){
		this.socketUrl = 'http://localhost:/quakesep';
		this.stompClient = null;
		this.socket = null;
	};
	WS.prototype = {
		init: function(){
			this.connectWS();
		},
		connectWS: function(){
			var _this = this;
			_this.socket = new SockJS(_this.socketUrl);
			_this.stompClient = Stomp.over(_this.socket);
			_this.stompClient.debug = true;
			_this.stompClient.connect({},
					function(frame){
						console.log('Connected: ' + frame);
						_this.stompClient.subscribe('/topic/quakes.all', function(message){
							_this.showImage(message.body);
						});
					}.bind(_this),
					function(e){
						console.error(e, "Reconnecting WS", this.socketUrl);
						window.setTimeout(function(){
							_this.connectWS();
						}.bind(_this), 500);
				}.bind(_this)
			);
		},
		showImage: function(data){
			$('#target').attr('src', 'data:image/jpg;base64,' + data);
		}
	};

	var ws = new WS();
	ws.init();
});
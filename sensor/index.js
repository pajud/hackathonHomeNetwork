"use strict";

// Run example:
//
// sudo npm start localhost:9000 mars3
//

var Cylon = require('cylon');
var http = require('http');
var request = require('request');

var server_url = 'http://'+process.argv[2];
var url_register = server_url + '/register';
var url_setName = function(id, name) {
	return server_url + '/setName?id=' + id + '&name=' + name;
};
var url_report = function(id, value) {
	return server_url + '/measuredata?id=' + id + '&data=' + value;
};

var bluetooth_names = {
	'mars2': '74daeab2bc56',
	'mars3': '74daeab2bc52',
}

var quantities = [
	'heading',
	'roll',
	'pitch',
	'temp',
	'pressure',
	'humidity',
];

// stat->gps_lat
// stat->gps_long
// stat->gps_fix_quality,
// stat->system_seconds,
// stat->v_bat,
// stat->v_solar,
// stat->i_in,
// stat->i_out,
// stat->imu_calib_status,
// stat->imu_heading,
// stat->imu_roll,
// stat->imu_pitch,
// stat->int_temperature,
// stat->int_pressure,
// stat->int_humidity,
// stat->ext_temperature,
// stat->ext_pressure,
// stat->ext_humidity,
// stat->accel_x,
// stat->accel_y,
// stat->accel_z,
// stat->speed,
// stat->altitude

var Sensor = function(name, on_registered) {
	this.name = name;
	var $this = this;

	request(url_register, function(err, response, content) {
		$this.sensor_id = content;
		console.log($this.name, ' register: ', content);

		var url = url_setName($this.sensor_id, name);
		request(url, function(err, response, content) {
			console.log($this.name, url, content);

			on_registered();
		});
	});
};

Sensor.prototype.report = function(value) {
	var $this = this;
	request(url_report($this.sensor_id, value), function(err, response, content) {
		//console.log('err', err, 'cnt', content);
		console.log($this.name, '(', $this.sensor_id, ')', ' <- ', value, err, content);
	});
};

var Device = function(name, bluetooth_name) {
	this.name = name;
	this.bluetooth_name = bluetooth_name;
	this.sensors = [];
	this.sensors_to_register = quantities.length;
	this.received_buffer = '';

	var device = this;

	for(var qty_idx = 0; qty_idx < quantities.length; qty_idx++)
	{
		var qty = quantities[qty_idx];
		this.sensors.push(new Sensor(this.name + '_' + qty, function() {
			device.on_sensor_registered();
		}));
	}
};

Device.prototype.on_sensor_registered = function() {
	this.sensors_to_register -= 1;
	var device = this;

	if(this.sensors_to_register <= 0) {
		//start robot handler
		Cylon.robot({
			name: this.name,
			connections: {
				bluetooth: { adaptor: 'ble', uuid: this.bluetooth_name }
			},

			devices: {
				octanis1_rover_ble: {
					driver: "ble-characteristic",
					serviceId: "ffe0", characteristicId: "ffe1",
					connection: "bluetooth"
				}
			},

			work: function(my) {
				my.octanis1_rover_ble.notifyCharacteristic(function(err, data) {
					if (err) {
						return console.error("NotifyError: ", err);
					}
					console.log("received: ", data.toString());
					device.on_packet_received(data);
				});
			}
		});

		Cylon.start();
	}
}

Device.prototype.on_packet_received = function(packet) {
	this.received_buffer += packet;

	this.try_send();
}

Device.prototype.try_send = function() {
	// now let's see if all values have been found
	var values = this.received_buffer.split(',');

	if(values.length == 20) {
		this.received_buffer = '';

		console.log(values);

		var measurement = {
			'heading': values[9],
			'roll': values[10],
			'pitch': values[11],
			'temp': values[12] / 100.0,
			'pressure': values[13],
			'humidity': values[14],
		};

		for(var qty_idx = 0; qty_idx < quantities.length; qty_idx++)
		{
			var qty = quantities[qty_idx];
			this.sensors[qty_idx].report(measurement[qty]);
		}

		//console.log(measurement);
	}
}

//init_sensor('mars2', mars2);
var name = process.argv[3];
new Device(name, bluetooth_names[name]);

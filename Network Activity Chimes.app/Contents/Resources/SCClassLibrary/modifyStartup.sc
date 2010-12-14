+ OSXPlatform {
	startupFiles {
//		var filename = "startup.rtf";
//		^[this.systemAppSupportDir +/+ filename, this.userAppSupportDir +/+ filename];
			// look for startup files inside the app Contents directory
		var filename = "startup.*";
		^(String.scDir +/+ filename).pathMatch;
	}

	startup {
		Document.implementationClass.startup;
		// make a server window for the internal if you like
		//Server.internal.makeWindow;
//		Server.local.makeWindow;
		// uncomment if you use a startup file
//		this.loadStartupFiles;
		// uncomment if you have multiple help files
//		Help.addToMenu;
	}
}

+ Main {
	startup {
		platform = this.platformClass.new;
		platform.initPlatform;

		super.startup;

		GUI.fromID( this.platform.defaultGUIScheme );
		GeneralHID.fromID( this.platform.defaultHIDScheme );
		
		//("python "++String.scDir.dirname++"/readstats.py &").unixCmd;

		// Set Server.default and the 's' interpreter variable to the internal server.
		// You should use the internal server for standalone applications --
		// otherwise, if your application has a problem, the user will
		// be stuck with a process, possibly making sound, that he won't know
		// how to kill.
		Server.default = Server.internal;
		interpreter.s = Server.default;

			// some folder paths that should point inside the app's Contents folder
		SynthDef.synthDefDir = String.scDir +/+ "synthdefs/";
		Archive.archiveDir = String.scDir;

		this.platform.startup;

		// from here on, you should customize what should happen...

		StartUp.run;

		// One can boot the server, then use .load to evaluate a file
		// OR - put things into a class... like the SCSA_Demo

		"Welcome to Standalone Demo made with SuperCollider, type cmd-d for help.".postln;

		Server.default.boot;

		Server.default.waitForBoot({
			
			SynthDef("networkSound", { arg inpack = 0, outpack = 0, mul = 0.12;
				var outArray;
				outArray = [
						Ringz.ar(Dust.ar(inpack*0.6, mul), 1600, 2),
						Ringz.ar(Dust.ar(outpack*0.6, mul), 2100, 2)
					];
				Out.ar(0, outArray)
			}, [0.15, 0.15]).load(Server.default);
			
			SynthDef("cpuSound", { arg cpuuser = 0, cpusys = 0, mul = 0.4;
				var outArray;
				outArray = [
					LPF.ar(
						Gendy3.ar(1,2,0.3,-0.7,75,0.03,0.1),
						(cpusys*30)+40,
						(cpusys / 200 )* mul
					),
					LPF.ar(
						Gendy3.ar(1,2,0.3,-0.7,150,0.03,0.1),
						(cpuuser*30)+40,
						(cpuuser / 200 )* mul
					),
				];
				Out.ar(0, outArray)
			}, [0.15,0.15]).load(Server.default);
			
			~aSynth = Synth("networkSound", [\inpack, 30, \outpack, 30, \mul, 0.12]);
			~bSynth = Synth("cpuSound", [\cpuuser, 40, \cpusys, 20, \mul, 0.4]);
			
			~w = Window("Volume controls for Network Activity Chimes", Rect(100, 500, 400, 140));
			
			~t = StaticText (~w, Rect(40, 0, 300, 30));
			~t.string = "Network Chimes Volume";
			
			~a = Slider (~w, Rect(40, 25, 300, 30));
			~a.value = 0.12;
			~a.action={ |sl| ~aSynth.set(\mul, sl.value) }; // set the action of the slider
			
			~u = StaticText (~w, Rect(40, 60, 300, 30));
			~u.string = "CPU Buzz Volume";
			
			~b = Slider (~w, Rect(40, 85, 300, 30));
			~b.value = 0.4;
			~b.action={ |sl| ~bSynth.set(\mul, sl.value) }; // set the action of the slider
			
			
			~r = Routine({
			
				9999999999.do({  // about 3.17 years
					0.1.wait; 				// pause for .1 second
					
					// You may need to change this file path to match the location of readstats.py.
					~x = FileReader.read(String.scDir.dirname++"/nets.txt", true, true);
					if ( ~x.notNil,
						{
							~x = ~x.collect(_.collect(_.interpret));
							//x.postcs;
							~aSynth.set(\inpack, ~x[0][0], \outpack, ~x[0][1]);
							~bSynth.set(\cpuuser, ~x[0][2], \cpusys, ~x[0][2]);
						},
						{}
					)
				})
				
			}).reset.play;
		
			~w.front;
			
			CmdPeriod.doOnce({~w.close});

			~netstats = "python Network\ Activity\ Chimes.app/Contents/getstats.py".unixCmdGetStdOut;
			~netstats.postln;
			
		});
		
		// close post window if user should not have it
		//Document.listener.close;
	}

}

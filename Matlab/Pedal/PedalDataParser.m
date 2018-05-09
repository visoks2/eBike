close all;
clear all;
clc;

% path = './Test 1/';
path = './New Folder/';
path = './20180508_panemunes tiltas/';
% path = './20180508_2/';


listing = dir(path);
for fileNo = 1:size(listing)-2
    file = strcat(path,listing(2+fileNo).name);
    fileID = fopen(file,'r');
    line_ex = fgetl(fileID);
    C{fileNo} = textscan(fileID,'%s %s 	%s	%s	%u');
    fclose('all');
end

% for fileNo = 1:size(listing)-2
%     for i = 1:size(C{fileNo}{2})
%     C{fileNo}{2}{i}(9) = '.';
%     C{fileNo}{2}{i}(13) = ' ';
%     end
% end


for fileNo = 1:size(listing)-2
    C{fileNo}{6} = datenum(C{fileNo}{2});
end

%     public static final String UUID_MODE_ID = "0000fff1-0000-1000-8000-00805f9b34fb";
%     public static final String UUID_PWM_DUTY_CYCLE_ID = "0000fff2-0000-1000-8000-00805f9b34fb";
%     public static final String UUID_V_THRESHOLD_ID = "0000fff3-0000-1000-8000-00805f9b34fb";


%     public static final String UUID_BIKE_BATTERY_LEVEL_ID = "0000fff4-0000-1000-8000-00805f9b34fb";
%     public static final String UUID_CURRENT_ID = "0000fff5-0000-1000-8000-00805f9b34fb";
%     public static final String UUID_BIKE_SPEED_ID = "0000fff6-0000-1000-8000-00805f9b34fb";
%     public static final String UUID_BIKE_FLAGS_ID = "0000fff7-0000-1000-8000-00805f9b34fb";



hold on
plot (C{3}{6}, str2double(C{3}{3}))
hold off 



Fs = 5000;            % Sampling frequency                    
T = 1/Fs;             % Sampling period       
L = 100000;             % Length of signal
t = (0:L-1)*T;        % Time vector
% plot(1000*t(1:50),X(1:50))
Y = fft(str2double(C{5}{3}));

P2 = abs(Y/L);
P1 = P2(1:L/2+1);
P1(2:end-1) = 2*P1(2:end-1);

f = Fs*(0:(L/2))/L;
plot(f,P1) 


a = 1;
b = [1 1];
y = filter(b,a,double(C{3}{5}));
plot(y)



% figure(2)
% plot(C{2}{5}-C{3}{5})
% 
% figure(2)
% hold on
% plot (C{2}{5})
% plot (C{3}{5})
% hold off 
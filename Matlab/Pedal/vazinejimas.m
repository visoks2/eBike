load('pasivazinejimas_visi')

T = cat(2, DD1.T, DD2.T, DD.T)
HR = cat(2, DD1.HR, DD2.HR, DD.HR)
Vbat = cat(2, DD1.Vbat, DD2.Vbat, DD.Vbat)
Current = cat(2, DD1.Current, DD2.Current, DD.Current)
Speed = cat(2, DD1.Speed, DD2.Speed, DD.Speed)
Power = cat(2, DD1.Power, DD2.Power, DD.Power)
PedalL = cat(2, DD1.PedalL, DD2.PedalL, DD.PedalL)
PedalR = cat(2, DD1.PedalR, DD2.PedalR, DD.PedalR)
Mode = cat(2, DD1.Mode, DD2.Mode, DD.Mode)
Duty = cat(2, DD1.Duty, DD2.Duty, DD.Duty)

x= find(Vbat<2000)
Vbat(x) = NaN;
Vbat = fillmissing(Vbat,'previous');

x= find(Current<500)
Current(x) = NaN;
Current = fillmissing(Current,'previous');

Current = (Current.*0.0217)-44.4+0.6;
Vbat = (Vbat.*0.01400529697297297297297297297297);

x = find(Current<0);
figure(1)
plot(T(x), Vbat(x), T(x), Current(x))


x = find(Current>0);
figure(2)
plot(T(x), Vbat(x), T(x), Current(x))
x= find(isnan(Current))
Current(x) = 0;
x= find(isnan(Vbat))
Vbat(x) = 0;


W = Current.*Vbat;

figure(5)
plot(T, W)
Wh = [];
s = size(W);
for i = 1 : s(2)
    Wh(i) = mean(W(1:i)) * ((T(i)-T(1))/hours(1));
end
figure(3)
plot(T, Wh)

x = find(Current>0);

W_notRegen = Current(x).*Vbat(x);
figure(4)
plot(T(x), W_notRegen)

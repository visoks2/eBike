#include "cmsis_os.h"

#include "stm32f4xx_hal.h"
#include "main.h"

#include <stdio.h>
#include <MC.h>
#include <stdlib.h>
#include <string.h>
#include <SDCard_Buffer.h>
#include "ff.h"
#include "SDCard.h"

#include "cJSON.h"

TaskHandle_t SDCard_Task_Handle = NULL;
void Start_SDCard_Task(void);

extern RTC_HandleTypeDef hrtc;

static FATFS FatFs;
static void SDCard_Task(void * pvParameters);

uint32_t fileCount = 0;
FIL Fil;
_Bool _closeFileFirst = 0;


void Start_SDCard_Task(void) {
	xTaskCreate(SDCard_Task,     				// Function that implements the task.
	"SDCard_Task",   				// Text name for the task.
	configMINIMAL_STACK_SIZE * 5,      	// Stack size in words, not bytes.
	(void *) 0,    				// Parameter passed into the task.
	configMAX_PRIORITIES - 2,				// Priority at which the task is created.
	&SDCard_Task_Handle);      	// Used to pass out the created task's handle.

	if (SDCard_Task_Handle == NULL)
		Error_Handler();

}

FRESULT ChangeFile() {
	FRESULT res;
	if (_closeFileFirst) {
		res = f_close(&Fil);
		if (res != FR_OK)
		{
			return res;
		}else
			_closeFileFirst = 0;
	}

	char data[30] = { 0 };
	RTC_TimeTypeDef RTC_TimeStructure;
	RTC_DateTypeDef RTC_DateStructure;
	HAL_RTC_GetTime(&hrtc, &RTC_TimeStructure, RTC_FORMAT_BIN);
	HAL_RTC_GetDate(&hrtc, &RTC_DateStructure, RTC_FORMAT_BIN);
	sprintf(data, "%02d%02d_%03d.txt", RTC_TimeStructure.Hours, RTC_TimeStructure.Minutes, fileCount++);

	res = f_open(&Fil, data, FA_OPEN_APPEND | FA_WRITE | FA_READ);

	return res;
}
extern char data[25];
extern uint16_t u_data[8000];
extern uint16_t i_data[8000];
extern int DataLimiter;
void SDCard_Task(void * pvParameters) {
	UNUSED(pvParameters);
	osDelay(1000);
	if (f_mount(&FatFs, "", 0) != FR_OK)
		Error_Handler();
	FRESULT res = FR_OK;
	UINT bw;
	uint8_t needChangeFile = 1;

	int a = 0;

	for (;;) {
		osDelay(25);
		if (needChangeFile)
		{
			if (ChangeFile() == FR_OK)
				needChangeFile = 0;
			else
			{
				HAL_GPIO_TogglePin(GPIOD, GPIO_PIN_12);
				osDelay(100);
			}
			HAL_GPIO_TogglePin(GPIOD, GPIO_PIN_12);
		}else if (Buffer_CanRead()) {
			//SDCardList_t* msg = Log();
			a++;
			//char* out = Buffer_GetString();


			res = f_write(&Fil, data, strlen(data), &bw);
			if (res != FR_OK)
				Error_Handler();


			int bytesCount = DataLimiter*2;

			int bytesToWrite = bytesCount;//msg->Length;
			char* tail = (char*)u_data;//msg->Data;
			//if (out == NULL)
			//	Error_Handler();
			while (bytesToWrite >=500)
			{
				res = f_write(&Fil, tail, 500, &bw);
				if (res != FR_OK)
					Error_Handler();
				tail+= 500;
				bytesToWrite-=500;
			}
			if (bytesToWrite > 0)
			{

				res = f_write(&Fil, tail, bytesToWrite, &bw);
				if (res != FR_OK)
					Error_Handler();
			}



			bytesToWrite = bytesCount;//msg->Length;
			tail = (char*)i_data;//msg->Data;
			//if (out == NULL)
			//	Error_Handler();
			while (bytesToWrite >=500)
			{
				res = f_write(&Fil, tail, 500, &bw);
				if (res != FR_OK)
					Error_Handler();
				tail+= 500;
				bytesToWrite-=500;
			}
			if (bytesToWrite > 0)
			{

				res = f_write(&Fil, tail, bytesToWrite, &bw);
				if (res != FR_OK)
					Error_Handler();
			}
			/*if (msg->Length != (int) bw)
				Error_Handler();*/

			//free(out);
			_closeFileFirst = 1;
			needChangeFile = 1;
			Buffer_OnReadFinish();
			HAL_GPIO_TogglePin(GPIOD, GPIO_PIN_12);

			//if (a % 20 == 0)
		}

		osDelay(25);
	}
}


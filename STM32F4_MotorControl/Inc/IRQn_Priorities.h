#ifndef IRQN_PRIORITIES_H_
#define IRQN_PRIORITIES_H_

#include "FreeRTOS.h"
#define USART_INTERRUPT_PRIORITY configLIBRARY_MAX_SYSCALL_INTERRUPT_PRIORITY + 5
#define EXT_INTERRUPT_PRIORITY configLIBRARY_MAX_SYSCALL_INTERRUPT_PRIORITY + 4
#define ADC_INTERRUPT_PRIORITY configLIBRARY_MAX_SYSCALL_INTERRUPT_PRIORITY + 3
#define SPI_SDCard_INTERRUPT_PRIORITY configLIBRARY_MAX_SYSCALL_INTERRUPT_PRIORITY + 6

#endif /* IRQN_PRIORITIES_H_ */

/**
 * This file is generated by xresloader 2.7.0, please don't edit it.
 * You can find more information about this xresloader on https://xresloader.atframe.work/ .
 * If there is any problem, please find or report issues on https://github.com/xresloader/xresloader/issues .
 */
#pragma once

#include "CoreMinimal.h"
#include "UObject/ConstructorHelpers.h"
#include "Engine/DataTable.h"
#include "Config/RoleCfg.h"
#include "Config/RoleUpgradeCfg.h"
#include "ArrInArr.generated.h"


USTRUCT(BlueprintType)
struct FArrInArr : public FTableRowBase
{
    GENERATED_USTRUCT_BODY()

    // Start of fields
    // This is a test name in array
    /** Field Type: STRING, Name: Name **/
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "XResConfig")
    FName Name;

    /** Field Type: INT, Name: IntArr **/
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "XResConfig")
    TArray< int32 > IntArr;

    /** Field Type: INT, Name: IntArr **/
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "XResConfig")
    TArray< int32 > IntArr;

    /** Field Type: STRING, Name: StrArr **/
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "XResConfig")
    TArray< FString > StrArr;

    /** Field Type: STRING, Name: StrArr **/
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "XResConfig")
    TArray< FString > StrArr;

    /** Field Type: MESSAGE, Name: TestInfoRole **/
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "XResConfig")
    FRoleCfg TestInfoRole;

    /** Field Type: MESSAGE, Name: TestRoleUpgradeCfg **/
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "XResConfig")
    FRoleUpgradeCfg TestRoleUpgradeCfg;

};


UCLASS(Blueprintable, BlueprintType)
class UArrInArrHelper : public UObject
{
    GENERATED_BODY()

public:
    UArrInArrHelper();

    static void ClearRow(FArrInArr& TableRow);

    UFUNCTION(BlueprintCallable, Category = "XResConfig")
    void ClearDataRow(FArrInArr& TableRow) const;

private:
    FArrInArr Empty;
};

